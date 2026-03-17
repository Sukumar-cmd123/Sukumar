package com.bank.service.impl;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;
import com.bank.entity.Customer;
import com.bank.entity.CustomerStatus;
import com.bank.exception.UnauthorizedAccessException;
import com.bank.repository.CustomerRepository;
import com.bank.security.AccessTokenBlacklistService;
import com.bank.security.JwtUtil;
import com.bank.service.AuthService;
import com.bank.service.OtpService;
import com.bank.service.RefreshTokenService;
import com.bank.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

    @Override
    public CheckCustomerResponse checkCustomer(String username, String mobileNumber) {

        boolean exists = customerRepository.existsByUsernameAndMobileNumber(username, mobileNumber);

        if (exists) {
            return new CheckCustomerResponse(true, "Customer exists. You can login.");
        }

        return new CheckCustomerResponse(false,
                "Customer not found. Please complete OTP verification to create account.");
    }

    @Override
    public void generateOtp(String username, String mobileNumber) {

        log.info("Generating OTP for username={} mobile={}", username, mobileNumber);

        otpService.generateOtp(username, mobileNumber);
    }

    @Override
    @Transactional //to mention fallbacks cases
    public void verifyOtpAndCreateAccount(String username,
                                          String mobileNumber,
                                          String otp,
                                          String rawPassword) {

        otpService.verifyOtp(username, mobileNumber, otp);

        if (customerRepository.existsByUsernameAndMobileNumber(username, mobileNumber)) {
            log.info("Customer already exists after OTP verification: {}", username);
            return;
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String accountNumber;
        int attempts = 0;

        do {
            accountNumber = accountNumberGenerator.generate();
            attempts++;
        }
        while (customerRepository.existsByAccountNumber(accountNumber) && attempts < 10);

        if (customerRepository.existsByAccountNumber(accountNumber)) {
            throw new IllegalStateException("Account number generation failed");
        }

        Customer customer = Customer.builder()
                .username(username)
                .mobileNumber(mobileNumber)
                .password(passwordEncoder.encode(rawPassword))
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .status(CustomerStatus.ACTIVE)
                .build();

        customerRepository.save(customer);

        log.info("Customer created successfully username={} accountNumber={}", username, accountNumber);
    }

    @Override
    public LoginResponse login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            log.info("Login attempt for username {}", username);
        } catch (AuthenticationException e) {
            throw new UnauthorizedAccessException("Invalid username or password");
        }
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid username or password"));
        String token = jwtUtil.generateToken(
                username,
                Map.of("accountNumber", customer.getAccountNumber())
        );
        String refreshToken = jwtUtil.generateRefreshToken(username);
        refreshTokenService.storeRefreshToken(username, refreshToken, 7 * 24 * 60 * 60); // 7 days in seconds
        return new LoginResponse(token, refreshToken, customer.getUsername(), customer.getAccountNumber());
    }

    @Override
    public MessageResponse logout(String refreshToken, String accessToken) {
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            return new MessageResponse("Invalid refresh token");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        String storedToken = refreshTokenService.getRefreshToken(username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            return new MessageResponse("Refresh token not found or mismatched");
        }
        refreshTokenService.deleteRefreshToken(username);
        if (accessToken != null && !accessToken.isBlank()) {
            accessTokenBlacklistService.blacklistToken(accessToken);
        }
        return new MessageResponse("Logout successful. Tokens expired.");
    }
}
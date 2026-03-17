package com.bank.controller;
import com.bank.dto.request.*;
import com.bank.audit.Auditable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.bank.service.AuthService;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.security.JwtUtil;
import com.bank.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

	@PostMapping("/check-customer")
	public ResponseEntity<CheckCustomerResponse> checkCustomer(@Valid @RequestBody CheckCustomerRequest request) {

        return ResponseEntity.ok(authService.checkCustomer(request.getUsername(), request.getMobileNumber()));
	}

	@PostMapping("/generate-otp")
	@Auditable(action = "GENERATE_OTP")
	public ResponseEntity<MessageResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
		authService.generateOtp(request.getUsername(), request.getMobileNumber());

        return ResponseEntity.ok(new MessageResponse("OTP sent successfully"));
	}

	@PostMapping("/verify-otp")
	@Auditable(action = "VERIFY_OTP_CREATE_CUSTOMER")
	public ResponseEntity<MessageResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		authService.verifyOtpAndCreateAccount(
				request.getUsername(),
				request.getMobileNumber(),
				request.getOtp(),
				request.getPassword()
		);

        return ResponseEntity.ok(new MessageResponse("OTP verified and customer account created"));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));

	}

	@PostMapping("/refresh-token")
	public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		String refreshToken = request.getRefreshToken();
		if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
			return ResponseEntity.status(401).build();
		}
		String username = jwtUtil.extractUsername(refreshToken);
		String storedToken = refreshTokenService.getRefreshToken(username);
		if (storedToken == null || !storedToken.equals(refreshToken)) {
			return ResponseEntity.status(401).build();
		}
		String newAccessToken = jwtUtil.generateToken(username, Map.of());
		// Optionally rotate refresh token:
		// String newRefreshToken = jwtUtil.generateRefreshToken(username);
		// refreshTokenService.storeRefreshToken(username, newRefreshToken, 7 * 24 * 60 * 60);
		// return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken, username, null));
		return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken, username, null));
	}

	@PostMapping("/logout")
	@Auditable(action = "LOGOUT")
	public ResponseEntity<MessageResponse> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestBody RefreshTokenRequest request) {
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        MessageResponse response = authService.logout(request.getRefreshToken(), accessToken);
        if (response.getMessage().toLowerCase().contains("invalid") || response.getMessage().toLowerCase().contains("not found")) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
    }
}

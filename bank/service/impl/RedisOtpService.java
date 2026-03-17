package com.bank.service.impl;
import com.bank.entity.OtpDetails;
import com.bank.exception.InvalidOtpException;
import com.bank.repository.OtpDetailsRepository;
import com.bank.service.OtpService;
import com.bank.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisOtpService implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(RedisOtpService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final OtpDetailsRepository otpDetailsRepository;
    private final SmsService smsService;

    @Value("${app.otp.ttl-seconds}")
    private long ttlSeconds;

    private static final int MAX_OTP_ATTEMPTS = 20;
    private static final int MAX_FAILED_VERIFY = 20;

    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(5);

    @Override
    @Transactional
    public void generateOtp(String username, String mobileNumber) {
        String redisKey = redisKey(username, mobileNumber);
        if (redisTemplate.hasKey(redisKey)) {
            throw new InvalidOtpException("OTP already sent. Please wait.");
        }
        String attemptKey = otpAttemptKey(username, mobileNumber);
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            throw new InvalidOtpException("Too many OTP requests. Try again later.");
        }
        redisTemplate.opsForValue().set(
                attemptKey,
                String.valueOf(attempts + 1),
                RATE_LIMIT_WINDOW
        );
        // ✅ Generate 4-digit numeric OTP
        String otp = generateNumericOtp();
        log.info("Generated OTP for {}: {}", username, otp);
        Instant expiry = Instant.now().plusSeconds(ttlSeconds);
        OtpDetails details = new OtpDetails(); // Always create new record
        details.setUsername(username);
        details.setMobileNumber(mobileNumber);
        details.setOtp(otp); // Store 4-digit OTP
        details.setExpiryTime(expiry);
        details.setVerified(false);
        details.setCreatedAt(Instant.now());
        try {
            otpDetailsRepository.save(details);
            otpDetailsRepository.flush(); // Force DB write for debugging
            log.info("OTP stored in DB for {} (ID: {})", username, details.getId());
        } catch (Exception e) {
            log.error("Failed to store OTP in DB for {}: {}", username, e.getMessage(), e);
            throw e;
        }
        // ✅ Send OTP via SMS
        log.debug("otp sms is being triggered");
        smsService.sendOtp(mobileNumber, otp);
        log.debug("otp sms is triggered");
        redisTemplate.opsForValue().set(
                redisKey,
                otp,
                Duration.ofSeconds(ttlSeconds)
        );
        log.info("OTP cached in Redis");
    }

    // Helper to generate 4-digit numeric OTP
    private String generateNumericOtp() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public void verifyOtp(String username, String mobileNumber, String otp) {
        Instant now = Instant.now();
        String redisKey = redisKey(username, mobileNumber);
        String cachedOtp = redisTemplate.opsForValue().get(redisKey);
        OtpDetails details = otpDetailsRepository
                .findByUsernameAndMobileNumber(username, mobileNumber)
                .orElseThrow(() -> new InvalidOtpException("OTP not found"));
        if (details.isVerified()) {
            throw new InvalidOtpException("OTP already verified");
        }
        if (details.getExpiryTime().isBefore(now)) {
            throw new InvalidOtpException("OTP expired");
        }
        boolean valid;
        if (cachedOtp != null) {
            valid = otp.equals(cachedOtp);
        } else {
            valid = otp.equals(details.getOtp());
        }
        if (!valid) {
            String failKey = otpFailKey(username);
            String failAttempts = redisTemplate.opsForValue().get(failKey);
            int attempts = failAttempts == null ? 0 : Integer.parseInt(failAttempts);
            if (attempts >= MAX_FAILED_VERIFY) {
                throw new InvalidOtpException("Too many invalid OTP attempts");
            }
            redisTemplate.opsForValue().set(
                    failKey,
                    String.valueOf(attempts + 1),
                    Duration.ofMinutes(10)
            );
            throw new InvalidOtpException("Invalid OTP");
        }
        details.setVerified(true);
        // Do NOT clear OTP after verification
        otpDetailsRepository.save(details);
        redisTemplate.delete(redisKey);

        log.info("OTP verified for {}", username);
    }

    private String redisKey(String username, String mobileNumber) {
        return "otp:" + username + ":" + mobileNumber;
    }

    private String otpAttemptKey(String username, String mobileNumber) {
        return "otp_attempts:" + username + ":" + mobileNumber;
    }

    private String otpFailKey(String username) {
        return "otp_fail:" + username;
    }
}

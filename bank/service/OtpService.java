package com.bank.service;

public interface OtpService {

    /**
     * Generates OTP, stores it in Redis + DB, and sends SMS.
     */
    void generateOtp(String username, String mobileNumber);

    /**
     * Verifies OTP for given username and mobile number.
     * Throws exception if invalid.
     */
    void verifyOtp(String username, String mobileNumber, String otp);
}
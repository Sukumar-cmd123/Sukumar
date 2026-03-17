package com.bank.service;
import com.bank.dto.response.CheckCustomerResponse;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.MessageResponse;

public interface AuthService {

	CheckCustomerResponse checkCustomer(String username, String mobileNumber);

	void generateOtp(String username, String mobileNumber);

	void verifyOtpAndCreateAccount(String username, String mobileNumber, String otp, String Password);

	LoginResponse login(String username, String password);

    MessageResponse logout(String refreshToken, String accessToken);
}

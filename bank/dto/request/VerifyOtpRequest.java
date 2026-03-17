package com.bank.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

	@NotBlank
	private String username;

	@NotBlank
	private String mobileNumber;

	@NotBlank
	private String otp;

    @NotBlank
	private String password;
}


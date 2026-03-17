package com.bank.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckCustomerRequest {
	@NotBlank
	private String username;

	@NotBlank
	private String mobileNumber;
}


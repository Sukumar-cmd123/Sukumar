package com.bank.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckCustomerResponse {
	private boolean exists;
	private String message;
}


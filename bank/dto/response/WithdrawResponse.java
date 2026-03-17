package com.bank.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WithdrawResponse {
	private String message;
	private BigDecimal balance;
}


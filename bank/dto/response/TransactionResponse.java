package com.bank.dto.response;
import com.bank.entity.TransactionStatus;
import com.bank.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TransactionResponse {
	private Long transactionId;
	private String accountNumber;
	private TransactionType transactionType;
	private BigDecimal amount;
	private BigDecimal balanceAfterTransaction;
	private Instant transactionDate;
	private TransactionStatus status;
}


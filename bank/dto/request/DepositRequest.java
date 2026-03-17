package com.bank.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
	@NotBlank(message = "accountNumber is required")
	private String accountNumber;

	@NotNull(message = "Amount shouldn't be null")
	@Positive(message = "Amount should be in Positive")
	private BigDecimal amount;
}


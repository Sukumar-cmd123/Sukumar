package com.bank.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawRequest {
	@NotBlank
	private String accountNumber;

	@NotNull
	@Positive
	private BigDecimal amount;
}


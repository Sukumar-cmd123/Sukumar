package com.bank.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
	@NotBlank
	private String fromAccount;

	@NotBlank
	private String toAccount;

	@NotNull
	@Positive
	private BigDecimal amount;
}


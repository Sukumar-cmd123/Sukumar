package com.bank.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transactions", indexes = {
		@Index(name = "idx_tx_account_date", columnList = "account_number,transaction_date")
})
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "account_number", nullable = false, length = 20)
	private String accountNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 30)
	private TransactionType transactionType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Column(name = "balance_after_transaction", nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceAfterTransaction;

	@Column(name = "transaction_date", nullable = false, updatable = false)
	private Instant transactionDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionStatus status;

	@PrePersist
	void onCreate() {
		if (transactionDate == null) transactionDate = Instant.now();
		if (status == null) status = TransactionStatus.SUCCESS;
	}
}


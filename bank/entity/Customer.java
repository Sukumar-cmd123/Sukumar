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
@Table(name = "customers",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_customers_username", columnNames = "username"),
				@UniqueConstraint(name = "uk_customers_mobile", columnNames = "mobile_number"),
				@UniqueConstraint(name = "uk_customers_account", columnNames = "account_number")
		})
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(unique = true,  name = "mobile_number", nullable = false, length = 15)
	private String mobileNumber;

	@Column(nullable = false, length = 225)
	private String password;

	@Column(name = "account_number", nullable = false, length = 20)
	private String accountNumber;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CustomerStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) createdAt = Instant.now();
		if (balance == null) balance = BigDecimal.ZERO;
		if (status == null) status = CustomerStatus.ACTIVE;
	}
}


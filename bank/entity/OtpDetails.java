package com.bank.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_details") // Removed unique constraint
public class OtpDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(name = "mobile_number", nullable = false, length = 15)
	private String mobileNumber;

	@Column( length = 10, nullable = true)
	private String otp;

	@Column(name = "expiry_time", nullable = false)
	private Instant expiryTime;

	@Column(nullable = false)
	private boolean verified;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

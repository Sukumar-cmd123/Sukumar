package com.bank.repository;
import com.bank.entity.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpDetailsRepository extends JpaRepository<OtpDetails, Long> {

    Optional<OtpDetails> findByUsernameAndMobileNumber(String username, String mobileNumber);

   // Optional<OtpDetails> findByUsernameAndMobileNumberAndOtp(String username, String mobileNumber, String otp);
}
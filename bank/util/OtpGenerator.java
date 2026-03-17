package com.bank.util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class OtpGenerator {
	private final SecureRandom secureRandom = new SecureRandom();
	private final int length;

	public OtpGenerator(@Value("${app.otp.length:6}") int length) {
		this.length = Math.max(4, Math.min(length, 10));
	}

	public String generate() {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(secureRandom.nextInt(10));
		}
		return sb.toString();
	}
}


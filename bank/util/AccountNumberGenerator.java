package com.bank.util;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
	private static final String PREFIX = "ACC";
	private static final int DIGITS = 6;
	private final SecureRandom secureRandom = new SecureRandom();

	public String generate() {
		int min = (int) Math.pow(10, DIGITS - 1);
		int maxExclusive = (int) Math.pow(10, DIGITS);
		int n = secureRandom.nextInt(maxExclusive - min) + min;
		return PREFIX + n;
	}
}


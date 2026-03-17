package com.bank.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
	private final SecretKey key;
	private final String issuer;
	private final long expirationMinutes;
	private final long refreshTokenExpirationMinutes = 7 * 24 * 60; // 7 days

	public JwtUtil(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.issuer:com.bank}") String issuer,
			@Value("${app.jwt.expiration-minutes:60}") long expirationMinutes
	) {
		byte[] bytes;
		try {
			bytes = Decoders.BASE64.decode(secret);
		} catch (Exception ignored) {
			// Accept raw secrets too (but production should use base64 or strong secret management).
			bytes = secret.getBytes();
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.issuer = issuer;
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(String subject, Map<String, Object> extraClaims) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(expirationMinutes * 60);
		return Jwts.builder()
				.subject(subject)
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.claims(extraClaims)
				.signWith(key)
				.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token) {
		Claims c = parseClaims(token);
		return c.getExpiration() != null && c.getExpiration().after(new Date());
	}

	public String generateRefreshToken(String subject) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(refreshTokenExpirationMinutes * 60);
		return Jwts.builder()
				.subject(subject)
				.issuer(issuer)
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(key)
				.compact();
	}

	public boolean isRefreshTokenValid(String token) {
		try {
			Claims claims = parseClaims(token);
			return claims.getExpiration() != null && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}
}

package com.bank.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class AccessTokenBlacklistService {
    private static final String BLACKLIST_PREFIX = "blacklisted:accessToken:";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    @Autowired
    public AccessTokenBlacklistService(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void blacklistToken(String token) {
        long expiryMillis = jwtUtil.parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        if (expiryMillis > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "1", expiryMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}

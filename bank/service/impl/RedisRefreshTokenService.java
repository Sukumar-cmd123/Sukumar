package com.bank.service.impl;

import com.bank.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService implements RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh_token:";

    @Override
    public void storeRefreshToken(String username, String refreshToken, long expirySeconds) {
        redisTemplate.opsForValue().set(PREFIX + username, refreshToken, expirySeconds, TimeUnit.SECONDS);
    }
    @Override
    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(PREFIX + username);
    }

    @Override
    public void deleteRefreshToken(String username) {
        redisTemplate.delete(PREFIX + username);
    }
}


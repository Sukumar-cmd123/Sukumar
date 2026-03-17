package com.bank.service;

public interface RefreshTokenService {
    void storeRefreshToken(String username, String refreshToken, long expirySeconds);
    String getRefreshToken(String username);
    void deleteRefreshToken(String username);
}


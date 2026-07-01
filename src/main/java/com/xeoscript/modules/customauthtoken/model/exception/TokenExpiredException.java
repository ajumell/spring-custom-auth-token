package com.xeoscript.modules.customauthtoken.model.exception;

import java.time.LocalDateTime;

public class TokenExpiredException extends RuntimeException {

    private final String token;
    private final LocalDateTime expiryTime;

    public TokenExpiredException(String token, LocalDateTime expiryTime) {
        super(String.format("Token expired (token: %s, expiry: %s)", maskToken(token), expiryTime));
        this.token = token;
        this.expiryTime = expiryTime;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return "***" + token.substring(token.length() - 4);
    }
}


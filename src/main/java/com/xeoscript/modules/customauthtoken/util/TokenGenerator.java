package com.xeoscript.modules.customauthtoken.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

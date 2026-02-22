package com.xeoscript.modules.customauthtoken.dao;

import com.xeoscript.modules.customauthtoken.jpa.entity.TokenEntity;

import java.time.LocalDateTime;

public interface TokenDAO {

    void save(TokenEntity entity);

    TokenEntity findByTokenValue(String tokenValue);

    long atomicValidateAndIncrement(String tokenValue, String parameter,
                                    String tokenType, LocalDateTime now);

    long invalidate(String tokenValue);

    long deleteExpired(LocalDateTime before);

    TokenEntity findByTokenValueAndParameter(String tokenValue, String parameter);
}

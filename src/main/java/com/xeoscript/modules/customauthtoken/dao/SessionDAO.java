package com.xeoscript.modules.customauthtoken.dao;

import com.xeoscript.modules.customauthtoken.jpa.entity.TokenSessionEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionDAO {

    void save(TokenSessionEntity entity);

    TokenSessionEntity findByTokenValueAndKey(String tokenValue, String sessionKey);

    List<TokenSessionEntity> findByTokenValue(String tokenValue);

    long deleteByTokenValueAndKey(String tokenValue, String sessionKey);

    long deleteByTokenValue(String tokenValue);

    long deleteExpiredByTokens(LocalDateTime before);
}


package com.xeoscript.modules.customauthtoken.services;

import com.xeoscript.modules.customauthtoken.dao.SessionDAO;
import com.xeoscript.modules.customauthtoken.dao.TokenDAO;
import com.xeoscript.modules.customauthtoken.jpa.entity.TokenEntity;
import com.xeoscript.modules.customauthtoken.jpa.entity.TokenSessionEntity;
import com.xeoscript.modules.customauthtoken.model.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionDAO sessionDAO;
    private final TokenDAO tokenDAO;

    @Override
    @Transactional
    public void save(String token, String key, String value) throws TokenExpiredException {
        validateTokenNotExpired(token);

        TokenSessionEntity existing = sessionDAO.findByTokenValueAndKey(token, key);
        TokenSessionEntity entity;

        if (existing != null) {
            existing.setSessionValue(value);
            entity = existing;
        } else {
            entity = new TokenSessionEntity();
            entity.setTokenValue(token);
            entity.setSessionKey(key);
            entity.setSessionValue(value);
        }

        sessionDAO.save(entity);
        log.debug("Session data stored for token '{}', key '{}'", maskToken(token), key);
    }

    @Override
    @Transactional(readOnly = true)
    public String get(String token, String key) throws TokenExpiredException {
        validateTokenNotExpired(token);

        TokenSessionEntity entity = sessionDAO.findByTokenValueAndKey(token, key);
        String value = entity != null ? entity.getSessionValue() : null;

        log.debug("Session data retrieved for token '{}', key '{}', found: {}",
                maskToken(token), key, value != null);

        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAll(String token) throws TokenExpiredException {
        validateTokenNotExpired(token);

        List<TokenSessionEntity> entities = sessionDAO.findByTokenValue(token);
        Map<String, String> result = new HashMap<>();

        for (TokenSessionEntity entity : entities) {
            result.put(entity.getSessionKey(), entity.getSessionValue());
        }

        log.debug("All session data retrieved for token '{}', found {} entries",
                maskToken(token), result.size());

        return result;
    }

    @Override
    @Transactional
    public boolean delete(String token, String key) throws TokenExpiredException {
        validateTokenNotExpired(token);

        long deletedCount = sessionDAO.deleteByTokenValueAndKey(token, key);
        boolean deleted = deletedCount > 0;

        log.debug("Session data deleted for token '{}', key '{}', deleted: {}",
                maskToken(token), key, deleted);

        return deleted;
    }

    @Override
    @Transactional
    public void clearAll(String token) throws TokenExpiredException {
        validateTokenNotExpired(token);

        long deletedCount = sessionDAO.deleteByTokenValue(token);

        log.debug("All session data cleared for token '{}', deleted {} entries",
                maskToken(token), deletedCount);
    }

    @Override
    @Transactional
    public long cleanupExpiredSessions(LocalDateTime before) {
        long deletedCount = sessionDAO.deleteExpiredByTokens(before);
        log.info("Cleaned up {} expired session records before {}", deletedCount, before);
        return deletedCount;
    }

    private void validateTokenNotExpired(String token) throws TokenExpiredException {
        TokenEntity entity = tokenDAO.findByTokenValue(token);

        if (entity == null) {
            throw new TokenExpiredException(token, LocalDateTime.now());
        }

        LocalDateTime now = LocalDateTime.now();
        if (!entity.getExpiryTime().isAfter(now)) {
            throw new TokenExpiredException(token, entity.getExpiryTime());
        }
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return "***" + token.substring(token.length() - 4);
    }
}


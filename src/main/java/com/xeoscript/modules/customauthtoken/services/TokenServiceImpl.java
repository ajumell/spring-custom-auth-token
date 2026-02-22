package com.xeoscript.modules.customauthtoken.services;

import com.xeoscript.modules.customauthtoken.config.TokenProperties;
import com.xeoscript.modules.customauthtoken.dao.TokenDAO;
import com.xeoscript.modules.customauthtoken.jpa.entity.TokenEntity;
import com.xeoscript.modules.customauthtoken.model.enums.HashingMode;
import com.xeoscript.modules.customauthtoken.model.enums.TokenStatus;
import com.xeoscript.modules.customauthtoken.model.enums.ValidationFailureReason;
import com.xeoscript.modules.customauthtoken.model.request.TokenRequest;
import com.xeoscript.modules.customauthtoken.model.request.ValidateTokenRequest;
import com.xeoscript.modules.customauthtoken.model.response.GeneratedToken;
import com.xeoscript.modules.customauthtoken.model.response.ValidationResult;
import com.xeoscript.modules.customauthtoken.util.TokenGenerator;
import com.xeoscript.modules.customauthtoken.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenDAO tokenDAO;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final TokenProperties properties;

    @Override
    @Transactional
    public GeneratedToken generate(TokenRequest request) {
        if (request.getParameter() == null || request.getParameter().trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter must not be null or blank");
        }

        Duration validity = request.getValidityDuration() != null
                ? request.getValidityDuration()
                : Duration.ofMinutes(properties.getDefaultValidityMinutes());

        Integer usageLimit;
        if (request.isUnlimitedUsage()) {
            usageLimit = null;
        } else if (request.getUsageLimit() != null) {
            usageLimit = request.getUsageLimit();
        } else {
            usageLimit = properties.getDefaultUsageLimit();
        }

        HashingMode hashingMode = request.getHashingMode() != null
                ? request.getHashingMode()
                : (properties.isHashingEnabled() ? HashingMode.SHA256 : HashingMode.NONE);

        String rawToken = tokenGenerator.generate(properties.getTokenLength());

        String storedValue = hashingMode == HashingMode.SHA256
                ? tokenHasher.hash(rawToken)
                : rawToken;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plus(validity);

        TokenEntity entity = new TokenEntity();
        entity.setTokenValue(storedValue);
        entity.setParameterValue(request.getParameter());
        entity.setTokenType(request.getTokenType());
        entity.setStatus(TokenStatus.ACTIVE);
        entity.setExpiryTime(expiryTime);
        entity.setUsageLimit(usageLimit);
        entity.setUsageCount(0);
        entity.setHashingMode(hashingMode.name());
        entity.setMetadata(request.getMetadata());

        tokenDAO.save(entity);

        log.debug("Token generated for parameter '{}', type '{}', expires at {}",
                request.getParameter(), request.getTokenType(), expiryTime);

        return GeneratedToken.builder()
                .token(rawToken)
                .expiryTime(expiryTime)
                .usageLimit(usageLimit)
                .remainingUses(usageLimit)
                .build();
    }

    @Override
    @Transactional
    public ValidationResult validate(ValidateTokenRequest request) {
        if (request.getParameter() == null || request.getParameter().trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter must not be null or blank");
        }
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }

        TokenEntity entity = tokenDAO.findByTokenValue(request.getToken());
        if (entity == null) {
            entity = tokenDAO.findByTokenValue(tokenHasher.hash(request.getToken()));
        }

        if (entity == null) {
            log.debug("Token not found for validation");
            return ValidationResult.builder()
                    .valid(false)
                    .failureReason(ValidationFailureReason.NOT_FOUND)
                    .build();
        }

        String storedTokenValue = entity.getTokenValue();
        LocalDateTime now = LocalDateTime.now();

        long updatedRows = tokenDAO.atomicValidateAndIncrement(
                storedTokenValue, request.getParameter(), request.getTokenType(), now);

        if (updatedRows == 0) {
            return buildFailureResult(entity, request, now);
        }

        TokenEntity updated = tokenDAO.findByTokenValueAndParameter(
                storedTokenValue, request.getParameter());

        Integer remainingUses = null;
        if (updated != null && updated.getUsageLimit() != null) {
            remainingUses = updated.getUsageLimit() - updated.getUsageCount();
        }

        log.debug("Token validated successfully for parameter '{}'", request.getParameter());

        return ValidationResult.builder()
                .valid(true)
                .remainingUses(remainingUses)
                .build();
    }

    @Override
    @Transactional
    public void invalidate(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }

        String storedValue = resolveStoredTokenValue(token);
        if (storedValue == null) {
            log.warn("Token not found for invalidation");
            return;
        }

        long updated = tokenDAO.invalidate(storedValue);
        if (updated == 0) {
            log.warn("Token could not be invalidated (not found or already invalidated)");
        } else {
            log.debug("Token invalidated successfully");
        }
    }

    @Override
    @Transactional
    public long cleanupExpired() {
        long deletedCount = tokenDAO.deleteExpired(LocalDateTime.now());
        log.info("Cleaned up {} expired tokens", deletedCount);
        return deletedCount;
    }

    private String resolveStoredTokenValue(String rawToken) {
        TokenEntity entity = tokenDAO.findByTokenValue(rawToken);
        if (entity != null) {
            return entity.getTokenValue();
        }

        entity = tokenDAO.findByTokenValue(tokenHasher.hash(rawToken));
        if (entity != null) {
            return entity.getTokenValue();
        }

        return null;
    }

    private ValidationResult buildFailureResult(TokenEntity entity, ValidateTokenRequest request,
                                                 LocalDateTime now) {
        ValidationFailureReason reason;

        if (entity.getStatus() == TokenStatus.USED) {
            reason = ValidationFailureReason.USAGE_LIMIT_EXCEEDED;
        } else if (entity.getStatus() == TokenStatus.INVALIDATED) {
            reason = ValidationFailureReason.INVALIDATED;
        } else if (!entity.getExpiryTime().isAfter(now)) {
            reason = ValidationFailureReason.EXPIRED;
        } else if (!entity.getParameterValue().equals(request.getParameter())) {
            reason = ValidationFailureReason.PARAMETER_MISMATCH;
        } else if (request.getTokenType() != null
                && !request.getTokenType().equals(entity.getTokenType())) {
            reason = ValidationFailureReason.TOKEN_TYPE_MISMATCH;
        } else if (entity.getUsageLimit() != null
                && entity.getUsageCount() >= entity.getUsageLimit()) {
            reason = ValidationFailureReason.USAGE_LIMIT_EXCEEDED;
        } else {
            reason = ValidationFailureReason.EXPIRED;
        }

        log.debug("Token validation failed: {}", reason);

        return ValidationResult.builder()
                .valid(false)
                .failureReason(reason)
                .build();
    }
}

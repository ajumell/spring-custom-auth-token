package com.xeoscript.modules.customauthtoken.services;

import com.xeoscript.modules.customauthtoken.model.request.TokenRequest;
import com.xeoscript.modules.customauthtoken.model.request.ValidateTokenRequest;
import com.xeoscript.modules.customauthtoken.model.response.GeneratedToken;
import com.xeoscript.modules.customauthtoken.model.response.ValidationResult;

public interface TokenService {

    GeneratedToken generate(TokenRequest request);

    ValidationResult validate(ValidateTokenRequest request);

    void invalidate(String token);

    long cleanupExpired();
}

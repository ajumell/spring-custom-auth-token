package com.xeoscript.modules.customauthtoken.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenRequest {

    private String parameter;

    private String token;

    private String tokenType;
}

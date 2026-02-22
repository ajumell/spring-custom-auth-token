package com.xeoscript.modules.customauthtoken.model.request;

import com.xeoscript.modules.customauthtoken.model.enums.HashingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest {

    private String parameter;

    private String tokenType;

    private Duration validityDuration;

    private Integer usageLimit;

    private boolean unlimitedUsage;

    private String metadata;

    private HashingMode hashingMode;
}

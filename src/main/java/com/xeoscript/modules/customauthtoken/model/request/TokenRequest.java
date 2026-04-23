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

    @Builder.Default
    private Duration validityDuration = Duration.ofMinutes(60);

    private Integer usageLimit;

    @Builder.Default
    private boolean unlimitedUsage = true;

    private String metadata;

    @Builder.Default
    private HashingMode hashingMode = HashingMode.SHA256;
}

package com.xeoscript.modules.customauthtoken.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.custom-auth-token")
public class TokenProperties {

    private int defaultValidityMinutes = 30;

    private int defaultUsageLimit = 1;

    private int tokenLength = 32;

    private boolean hashingEnabled = false;

    private boolean cleanupEnabled = false;

    private boolean allowMultipleActive = true;
}

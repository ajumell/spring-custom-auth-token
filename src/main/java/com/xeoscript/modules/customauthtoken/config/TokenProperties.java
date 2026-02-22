package com.xeoscript.modules.customauthtoken.config;

import com.xeoscript.env.Config;
import com.xeoscript.env.EnvConfiguration;

@EnvConfiguration(prefix = "spring.custom-auth-token")
public interface TokenProperties {

    @Config(name = "default-validity-minutes", value = "30", required = false)
    int getDefaultValidityMinutes();

    @Config(name = "default-usage-limit", value = "1", required = false)
    int getDefaultUsageLimit();

    @Config(name = "token-length", value = "32", required = false)
    int getTokenLength();

    @Config(name = "hashing-enabled", value = "false", required = false)
    boolean isHashingEnabled();

    @Config(name = "cleanup-enabled", value = "false", required = false)
    boolean isCleanupEnabled();

    @Config(name = "allow-multiple-active", value = "true", required = false)
    boolean isAllowMultipleActive();
}

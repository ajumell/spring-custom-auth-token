package com.xeoscript.modules.customauthtoken.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.xeoscript.modules.customauthtoken")
@EntityScan(basePackages = {"com.xeoscript.modules.customauthtoken.jpa.entity"})
public class CustomAuthTokenAutoConfiguration {

}

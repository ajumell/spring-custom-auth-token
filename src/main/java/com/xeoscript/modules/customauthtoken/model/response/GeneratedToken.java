package com.xeoscript.modules.customauthtoken.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedToken {

    private String token;

    private LocalDateTime expiryTime;

    private Integer usageLimit;

    private Integer remainingUses;
}

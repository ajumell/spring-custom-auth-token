package com.xeoscript.modules.customauthtoken.model.response;

import com.xeoscript.modules.customauthtoken.model.enums.ValidationFailureReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    private boolean valid;

    private ValidationFailureReason failureReason;

    private Integer remainingUses;
}

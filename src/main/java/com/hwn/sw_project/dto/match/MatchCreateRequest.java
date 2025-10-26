package com.hwn.sw_project.dto.match;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MatchCreateRequest(
        @NotNull Long userId,
        @NotNull Long benefitId,

        @DecimalMin(value = "0.0", inclusive = true)
        @Digits(integer = 3, fraction = 3)
        BigDecimal score
) {}

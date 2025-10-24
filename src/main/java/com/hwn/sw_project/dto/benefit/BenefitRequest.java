package com.hwn.sw_project.dto.benefit;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BenefitRequest(
        @NotBlank String title,
        @NotNull Long categoryId,
        @NotNull Long providerId,
        LocalDate validFrom,
        LocalDate validTo
) {}

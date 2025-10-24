package com.hwn.sw_project.dto.benefit;

import java.time.LocalDate;

public record BenefitResponse(
        Long benefitId,
        String title,
        Long categoryId,
        String categoryName,
        Long providerId,
        String providerName,
        LocalDate validFrom,
        LocalDate validTo,
        String status
) {}

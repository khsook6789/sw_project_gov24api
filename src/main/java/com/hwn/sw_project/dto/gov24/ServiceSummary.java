package com.hwn.sw_project.dto.gov24;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public record ServiceSummary(
        String svcId,
        String title,
        String providerName,
        String category,
        String summary,
        String detailUrl,
        String applyPeriod,
        String applyMethod,
        LocalDate regDate,
        LocalDate deadline,
        Long viewCount
) {}
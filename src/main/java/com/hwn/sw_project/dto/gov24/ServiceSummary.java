package com.hwn.sw_project.dto.gov24;

public record ServiceSummary(
        String svcId,
        String title,
        String providerName,
        String category,
        String summary,
        String detailUrl,
        String applyPeriod,
        String applyMethod
) {}
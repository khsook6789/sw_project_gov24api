package com.hwn.sw_project.dto.gov24;

import java.util.List;

public record RecommendationItem(
        String svcId,
        String title,
        String providerName,
        String category,
        String summary,
        String applyPeriod,
        String applyMethod,
        double score,
        List<String> matchedReasons
) {}

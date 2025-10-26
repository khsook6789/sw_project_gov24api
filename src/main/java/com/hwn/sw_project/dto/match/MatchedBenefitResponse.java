package com.hwn.sw_project.dto.match;

import java.math.BigDecimal;
import java.time.Instant;

public record MatchedBenefitResponse(
        Long matchId,
        Long userId,
        Long benefitId,
        String benefitTitle,
        BigDecimal score,
        Instant matchedAt
) {}

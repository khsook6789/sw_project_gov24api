package com.hwn.sw_project.dto.match;

import java.time.Instant;

public record MatchedBenefitResponse(
        Long matchId,
        Long userId,
        Long benefitId,
        String benefitTitle,
        Double score,
        Instant matchedAt
) {}

package com.hwn.sw_project.dto.interest;

import java.time.Instant;

public record InterestResponse(
        String tag,
        Instant createdAt
) {}

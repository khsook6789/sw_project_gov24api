package com.hwn.sw_project.dto.provider;

import java.time.Instant;

public record ProviderResponse(
        Long providerId,
        String name,
        String type,
        String regionCode,
        Instant createdAt,
        Instant updatedAt
){}

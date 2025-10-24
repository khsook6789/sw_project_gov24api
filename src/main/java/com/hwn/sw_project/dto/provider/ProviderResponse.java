package com.hwn.sw_project.dto.provider;

public record ProviderResponse(
        Long providerId,
        String name,
        String type,
        String regionCode,
        String regionName
){}

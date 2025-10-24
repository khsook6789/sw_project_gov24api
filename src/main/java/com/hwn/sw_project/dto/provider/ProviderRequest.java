package com.hwn.sw_project.dto.provider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProviderRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String type,
        @Size(max = 10) String regionCode
) {}

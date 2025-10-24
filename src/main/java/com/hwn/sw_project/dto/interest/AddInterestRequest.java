package com.hwn.sw_project.dto.interest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddInterestRequest(
        @NotBlank @Size(max = 50) String tag
) {}

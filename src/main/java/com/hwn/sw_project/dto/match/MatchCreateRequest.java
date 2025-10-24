package com.hwn.sw_project.dto.match;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchCreateRequest(
        @NotNull Long userId,
        @NotNull Long benefitId,
        @Min(0) Double score
) {}

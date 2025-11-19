package com.hwn.sw_project.dto.gov24;

import jakarta.validation.constraints.*;

import java.util.List;

public record UserProfile(
        @Min(0) @Max(120) Integer age,
        @Pattern(regexp = "M|F") String gender,
        @Pattern(regexp = "0-50|51-75|76-100|101-200|200\\+") String incomeBracket,
        List<String> specialFlags,
        String studentStatus,
        String employmentStatus,
        String industry,
        String category
) {}

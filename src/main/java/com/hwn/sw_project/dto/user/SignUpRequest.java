package com.hwn.sw_project.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 6, max = 100) String password
) {}

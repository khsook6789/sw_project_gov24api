package com.hwn.sw_project.dto.category;

import jakarta.validation.constraints.*;

public record CategoryRequest(
        @NotBlank @Size(max = 100) String name
) {}

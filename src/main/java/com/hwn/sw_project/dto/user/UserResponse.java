package com.hwn.sw_project.dto.user;

public record UserResponse(
        Long userId,
        String email,
        String username
) {}
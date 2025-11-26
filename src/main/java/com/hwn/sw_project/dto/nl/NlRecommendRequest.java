package com.hwn.sw_project.dto.nl;

public record NlRecommendRequest(
        String query,
        Integer top
) {
}

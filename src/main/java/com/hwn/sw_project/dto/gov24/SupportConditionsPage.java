package com.hwn.sw_project.dto.gov24;

import java.util.List;

public record SupportConditionsPage(
        Integer page,
        Integer perPage,
        Long totalCount,
        Integer currentCount,
        Integer matchCount,
        List<SupportConditionsDTO> data
) {}

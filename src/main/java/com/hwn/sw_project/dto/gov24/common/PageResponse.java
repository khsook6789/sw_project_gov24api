package com.hwn.sw_project.dto.gov24.common;
import java.util.List;

public record PageResponse<T>(
        int page,
        int perPage,
        int currentCount,
        long totalCount,
        List<T> data
){}

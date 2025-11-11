package com.hwn.sw_project.dto.common;
import java.util.List;

public record PageResponse<T>(
        int page,
        int perPage,
        int currentCount,
        long totalCount,
        List<T> data
){}

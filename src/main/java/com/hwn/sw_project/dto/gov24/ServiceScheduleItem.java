package com.hwn.sw_project.dto.gov24;

import java.time.LocalDate;

public record ServiceScheduleItem(
        String svcId,
        String title,
        String providerName,
        String category,
        String applyPeriod,
        LocalDate deadline
) {
}

package com.hwn.sw_project.dto.nl;

import java.util.List;

public record NlParsedQuery(
        Integer age,
        String gender,          // "M" / "F" / null
        String incomeBracket,   // "0-50","51-75","76-100","101-200","200+" 또는 null
        List<String> specialFlags,
        String studentStatus,
        String employmentStatus,
        String industry,
        String category,        // "보육·교육","복지·일상" 등 또는 null
        List<String> keywords   // ["등록금","생활비","청년"] 등
) {}

package com.hwn.sw_project.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gov24DateParser {

    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd."),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    // 문장 안에 있는 첫 번째 날짜 형태만 뽑아서 파싱
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(\\d{4}[./-]\\d{1,2}[./-]\\d{1,2})");

    public static LocalDate parseSingleDate(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isBlank()) return null;

        // "상시신청" 같은 경우는 날짜가 아니라서 null 리턴
        if (v.contains("상시")) return null;

        // "2025.01.01.~2025.12.31." 같은 문장에서 첫 날짜만 추출
        Matcher m = DATE_PATTERN.matcher(v);
        if (m.find()) {
            v = m.group(1);
        }

        for (DateTimeFormatter f : FORMATTERS) {
            try {
                return LocalDate.parse(v, f);
            } catch (Exception ignored) {}
        }
        return null;
    }
}

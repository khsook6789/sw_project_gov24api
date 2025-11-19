package com.hwn.sw_project.dto.gov24;

public record SupportConditionsDTO(
        String 서비스ID,
        String JA0101, String JA0102,   // 남, 여
        Integer JA0110, Integer JA0111, // 나이 from/to
        String JA0201, String JA0202, String JA0203, String JA0204, String JA0205, // 소득
        String JA0301, String JA0302, String JA0303, // 예비/임산부/출산
        String JA0328, String JA0329, String JA0330, // 장애/보훈/질환
        String JA0401, String JA0402, String JA0403, String JA0404, String JA0411, // 가구
        String JA0317, String JA0318, String JA0319, String JA0320, // 학생
        String JA0326, String JA0327, // 취업여부
        String JA1201, String JA1202, String JA1299, // 개인 업종
        String JA2201, String JA2202, String JA2203, String JA2299 // 법인 업종
) {}

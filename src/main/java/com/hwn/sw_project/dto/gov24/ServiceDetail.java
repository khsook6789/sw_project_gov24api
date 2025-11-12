package com.hwn.sw_project.dto.gov24;

import java.util.Map;

public record ServiceDetail(
        String svcId,             // 서비스ID
        String title,             // 서비스명
        String summary,           // 서비스목적 (또는 목적요약)
        String providerName,      // 소관기관명
        String category,          // 서비스분야 (상세에 없으면 null일 수 있음)
        String applyPeriod,       // 신청기한
        String applyMethod,       // 신청방법
        String requiredDocs,      // 구비서류
        String inquiry,           // 문의처
        String law,               // 법령
        String localRegulation,   // 자치법규
        String adminRule,         // 행정규칙
        String homepage,          // 온라인신청사이트URL
        String supportType,       // 지원유형
        String receiveOrg,        // 접수기관명
        String updatedAt,         // 수정일시
        String supportTarget,     // 지원대상
        String supportContent,    // 지원내용
        String selectionCriteria, // 선정기준
        String detailUrl,         // 상세조회URL(상세엔 없을 수 있으니 null 허용)
        Map<String, String> raw   // 원문 전체(한글 키-값)
) {}
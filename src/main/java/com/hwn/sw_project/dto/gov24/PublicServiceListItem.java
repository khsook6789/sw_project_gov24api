package com.hwn.sw_project.dto.gov24;

import lombok.Data;

@Data
public class PublicServiceListItem {
    private String serviceId;          // 서비스ID
    private String serviceName;        // 서비스명
    private String purposeSummary;     // 서비스목적요약
    private String field;              // 서비스분야
    private String ownerOrgName;       // 소관기관명
    private String ownerOrgType;       // 소관기관유형
    private String detailUrl;          // 상세조회URL
    private String applyPeriod;        // 신청기한
    private String applyMethod;        // 신청방법
    private String contact;            // 전화문의
    private String receiveOrg;         // 접수기관
    private String supportType;        // 지원유형
    private String updatedAt;          // 수정일시
    private Integer viewCount;         // 조회수
}
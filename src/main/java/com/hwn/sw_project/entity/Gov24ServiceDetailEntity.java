package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gov24_service_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gov24ServiceDetailEntity {
    @Id
    @Column(name = "svc_id", length = 30)
    private String svcId;

    // -------- 검색용 주요 필드들 --------
    @Column(name = "support_target", columnDefinition = "TEXT")
    private String supportTarget;       // 지원대상

    @Column(name = "support_content", columnDefinition = "TEXT")
    private String supportContent;      // 지원내용

    @Column(name = "selection_criteria", columnDefinition = "TEXT")
    private String selectionCriteria;   // 선정기준

    @Column(name = "required_docs", columnDefinition = "TEXT")
    private String requiredDocs;        // 구비서류

    @Column(name = "inquiry", columnDefinition = "TEXT")
    private String inquiry;             // 문의처

    @Column(name = "law", columnDefinition = "TEXT")
    private String law;                 // 법령

    @Column(name = "local_regulation", columnDefinition = "TEXT")
    private String localRegulation;     // 자치법규

    @Column(name = "admin_rule", columnDefinition = "TEXT")
    private String adminRule;           // 행정규칙

    @Column(name = "homepage", length = 255)
    private String homepage;            // 온라인신청사이트URL

    @Column(name = "support_type", length = 100)
    private String supportType;         // 지원유형

    @Column(name = "receive_org", length = 255)
    private String receiveOrg;          // 접수기관명

    // -------- serviceDetail의 "수정일시" --------
    @Column(name = "api_updated_at")
    private LocalDate apiUpdatedAt; // 수정일시를 파싱해서 보관

    // 원문 전체 (한글 키-값 Map<String,String>)을 JSON으로 직렬화해서 저장
    @Lob
    @Column(name = "raw_json")
    private String rawJson;
}

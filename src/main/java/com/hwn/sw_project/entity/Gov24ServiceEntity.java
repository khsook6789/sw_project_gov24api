package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gov24_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gov24ServiceEntity {
    @Id
    @Column(name = "svc_id", length = 30)
    private String svcId;

    @Column(nullable = false,length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "provider_name",length = 100)
    private String providerName;

    @Column(length = 100)
    private String category;

    @Column(name = "apply_period", length = 255)
    private String applyPeriod;

    @Column(name = "apply_method", length = 255)
    private String applyMethod;

    @Column(name = "detail_url", length = 255)
    private String detailUrl;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "registered_at")
    private LocalDate regDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "svc_id", referencedColumnName = "svc_id", insertable = false, updatable = false)
    private Gov24ServiceDetailEntity detail;
}

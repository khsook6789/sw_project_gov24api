package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_matched_benefit",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_benefit", columnNames = {"user_id","benefit_id"})
)
public class UserMatchedBenefit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long matchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id")
    private Benefit benefit;

    @Column(precision = 6, scale = 3)
    private Double score;

    @Column(name = "matched_at", nullable = false)
    private Instant matchedAt = Instant.now();
}

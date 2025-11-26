package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_favorite_user_svc", columnNames = {"user_id", "svc_id"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "svc_id", nullable = false, length = 20)
    private String svcId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

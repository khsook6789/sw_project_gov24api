package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false,length = 50)
    private String username; //사용자명

    @Column(unique = true, nullable = false, length = 50)
    private String email; //기존 username -> email

    @Column(nullable = false)
    private String password;

    // 🔹 소셜 로그인용 필드 추가
    @Column(length = 20)
    private String provider;   // 예: "NAVER"

    @Column(length = 100)
    private String providerId; // 네이버에서 주는 고유 id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Column(name = "created_at", nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

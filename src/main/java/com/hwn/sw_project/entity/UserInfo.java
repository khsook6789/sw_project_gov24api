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
@Table(name = "user_info")
public class UserInfo {
    @Id
    @Column(name = "user_id")
    private Long UserId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(precision = 12, scale = 2)
    private Double income;

    @Column(length = 100)
    private String job;

    @ManyToOne
    @JoinColumn(name = "region_code")
    private Region region;

    @Column(name = "created_at", nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public void onUpdate(){
        this.updatedAt = Instant.now();
    }

    public enum Gender{male,female}
}

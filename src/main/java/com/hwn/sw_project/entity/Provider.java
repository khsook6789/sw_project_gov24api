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
@Table(name = "provider")
public class Provider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Long providerId;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(length = 50)
    private String type;

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
}

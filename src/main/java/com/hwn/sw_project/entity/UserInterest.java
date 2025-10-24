package com.hwn.sw_project.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_interest")
public class UserInterest {
    @EmbeddedId
    private UserInterestId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "created_at", nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UserInterestId implements java.io.Serializable{
        private Long userId;
        private String tag;
    }
}

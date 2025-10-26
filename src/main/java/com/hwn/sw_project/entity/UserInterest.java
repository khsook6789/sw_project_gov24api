package com.hwn.sw_project.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

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
    @JoinColumn(name = "user_id",nullable = false)
    private AppUser user;

    @Builder.Default
    @Column(name = "created_at", nullable = false,updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class UserInterestId implements java.io.Serializable{
        @Column(name = "user_id",nullable = false)
        private Long userId;

        @Column(name = "tag",length = 50, nullable = false)
        private String tag;

        @Override
        public boolean equals(Object o){
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserInterestId that = (UserInterestId) o;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode(){
            return Objects.hash(userId,tag);
        }
    }
}

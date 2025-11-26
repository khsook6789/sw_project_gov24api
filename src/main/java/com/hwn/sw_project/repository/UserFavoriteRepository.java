package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    boolean existsByUserAndSvcId(AppUser user, String svcId);

    Optional<UserFavorite> findByUserAndSvcId(AppUser user, String svcId);

    List<UserFavorite> findByUserOrderByCreatedAtDesc(AppUser user);
}

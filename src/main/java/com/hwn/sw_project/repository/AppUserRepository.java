package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    // email 기반 조회/존재확인 메서드 추가
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<AppUser> findByProviderAndProviderId(String provider, String providerId);
}
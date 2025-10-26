package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo,Long> {
    Optional<UserInfo> findByUser(AppUser user);
    Optional<UserInfo> findByUserUserId(Long userId);
}

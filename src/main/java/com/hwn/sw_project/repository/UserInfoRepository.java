package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo,Long> {

}

package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.UserInterest;
import com.hwn.sw_project.entity.UserInterest.UserInterestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {
    List<UserInterest> findByIdUserId(Long userId);
    List<UserInterest> findByIdTag(String tag);

    boolean existsById(UserInterestId id);
    long deleteByIdUserIdAndIdTag(Long userId, String tag);
}

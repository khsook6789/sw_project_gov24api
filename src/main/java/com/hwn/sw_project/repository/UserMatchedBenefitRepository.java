package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.UserMatchedBenefit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMatchedBenefitRepository extends JpaRepository<UserMatchedBenefit, Long> {
    Page<UserMatchedBenefit> findByUser_UserId(Long userId, Pageable pageable);
    Page<UserMatchedBenefit> findByBenefit_BenefitId(Long benefitId, Pageable pageable);

    boolean existsByUser_UserIdAndBenefit_BenefitId(Long userId, Long benefitId);
    Long deleteByUser_UserIdAndBenefit_BenefitId(Long userId, Long benefitId);
}

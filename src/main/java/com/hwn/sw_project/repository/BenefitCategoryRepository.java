package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.BenefitCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;

import java.util.Optional;

public interface BenefitCategoryRepository extends JpaRepository<BenefitCategory,Long> {
    Optional<BenefitCategory> findByName(String name);
    boolean existsByName(String name);
    Page<BenefitCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

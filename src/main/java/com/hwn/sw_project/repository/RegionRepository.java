package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region,String> {
    boolean existsByRegionCode(String regionCode);
}

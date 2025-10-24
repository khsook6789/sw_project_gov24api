package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.Provider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
    Page<Provider> findByRegion_RegionCode(String regionCode, Pageable pageable);
    Page<Provider> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameAndRegion_RegionCode(String name, String regionCode);
}

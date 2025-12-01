package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.Gov24ServiceDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Gov24ServiceDetailRepository extends JpaRepository<Gov24ServiceDetailEntity, String> {
    // svcId 여러 개 한 번에 조회용
    List<Gov24ServiceDetailEntity> findBySvcIdIn(List<String> svcIds);
}

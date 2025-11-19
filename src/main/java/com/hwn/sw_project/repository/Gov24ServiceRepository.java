package com.hwn.sw_project.repository;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Gov24ServiceRepository extends JpaRepository<Gov24ServiceEntity, String> {
    // 카테고리 필터링
    Page<Gov24ServiceEntity> findByCategory(String category, Pageable pageable);

    //제목 검색
    Page<Gov24ServiceEntity> findByTitleContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    // 검색 + 카테고리 필터링
    Page<Gov24ServiceEntity> findByCategoryAndTitleContainingIgnoreCase(
            String category,
            String keyword,
            Pageable pageable
    );
}

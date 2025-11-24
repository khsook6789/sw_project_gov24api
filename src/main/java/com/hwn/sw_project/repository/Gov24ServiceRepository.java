package com.hwn.sw_project.repository;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
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

    @Query("""
    SELECT g FROM Gov24ServiceEntity g
    LEFT JOIN g.detail d
    ORDER BY d.apiUpdatedAt DESC NULLS LAST
""")
    Page<Gov24ServiceEntity> findAllOrderByUpdated(Pageable pageable);

    // 🔥 serviceList + serviceDetail 모두에서 검색 + 수정일시(apiUpdatedAt) 최신순 정렬
    @Query("""
        SELECT g
        FROM Gov24ServiceEntity g
        LEFT JOIN g.detail d
        WHERE
          (:keyword IS NULL OR :keyword = '' OR
             LOWER(g.title)        LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(g.summary)      LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(g.category)     LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(g.providerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(g.applyMethod)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(d.supportTarget)     LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(d.supportContent)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(d.selectionCriteria) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(d.requiredDocs)      LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(d.inquiry)           LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:category IS NULL OR :category = '' OR g.category = :category)
        """)
    Page<Gov24ServiceEntity> search(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );
}

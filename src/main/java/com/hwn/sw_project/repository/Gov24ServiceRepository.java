package com.hwn.sw_project.repository;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface Gov24ServiceRepository extends JpaRepository<Gov24ServiceEntity, String> {
    Optional<Gov24ServiceEntity> findBySvcId(String svcId);

    // 카테고리 필터링
    Page<Gov24ServiceEntity> findByCategory(String category, Pageable pageable);

    // svcId 목록으로 한 번에 조회
    List<Gov24ServiceEntity> findBySvcIdIn(Collection<String> svcIds);

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

    //   같은 카테고리 + 자기 자신 제외
    //   정렬 우선순위:
    //   1) providerName이 기준 서비스와 같은 것 먼저
    //   2) 그 안에서 viewCount 내림차순
    //   3) tie-breaker로 svcId 오름차순
    @Query("""
        SELECT g
        FROM Gov24ServiceEntity g
        WHERE g.category = :category
          AND g.svcId <> :svcId
        ORDER BY
          CASE 
            WHEN (:providerName IS NOT NULL AND g.providerName = :providerName) THEN 0 
            ELSE 1 
          END,
          g.viewCount DESC,
          g.svcId ASC
        """)
    Page<Gov24ServiceEntity> findSimilarByCategoryAndProvider(
            @Param("category") String category,
            @Param("svcId") String svcId,
            @Param("providerName") String providerName,
            Pageable pageable
    );

    // deadline NOT NULL 인 것만
    Page<Gov24ServiceEntity> findByDeadlineIsNotNull(Pageable pageable);

    @Query("""
    SELECT g FROM Gov24ServiceEntity g
    LEFT JOIN g.detail d
    ORDER BY d.apiUpdatedAt DESC NULLS LAST
""")
    Page<Gov24ServiceEntity> findAllOrderByUpdated(Pageable pageable);

    // serviceList + serviceDetail 모두에서 검색 + 수정일시(apiUpdatedAt) 최신순 정렬
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

    @Modifying
    @Query("update Gov24ServiceEntity g set g.viewCount = g.viewCount + 1 " +
            "where g.svcId = :svcId")
    int incrementViewCount(@Param("svcId") String svcId);
}

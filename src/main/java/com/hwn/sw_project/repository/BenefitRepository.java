package com.hwn.sw_project.repository;

import com.hwn.sw_project.entity.Benefit;
import com.hwn.sw_project.entity.Benefit.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {
    Page<Benefit> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    Page<Benefit> findByProvider_ProviderId(Long providerId, Pageable pageable);

    // 유효기간 및 상태 기준 검색
    Page<Benefit> findByStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Status status, LocalDate from, LocalDate to, Pageable pageable);
    // 날짜 기준 유효 혜택 검색
    default Page<Benefit> findActiveOn(LocalDate date, Pageable pageable){
        return findByStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(Status.active, date, date, pageable);
    }

    //제목 기준 검색
    Page<Benefit> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByProvider_ProviderIdandTitle(Long providerId,String Title);

    long countByCategory_CategoryId(Long categoryId);
}

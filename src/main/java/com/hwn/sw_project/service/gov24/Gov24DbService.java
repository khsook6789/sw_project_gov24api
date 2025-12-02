package com.hwn.sw_project.service.gov24;

import com.hwn.sw_project.dto.gov24.ServiceScheduleItem;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Gov24DbService {
    private final Gov24ServiceRepository repo;

    /**
     * DB에 저장된 gov24_service에서 검색/정렬/카테고리 필터링
     */
    public PageResponse<ServiceSummary> searchFromDb(
       Integer page,
       Integer size,
       String keyword,
       String category,
       String sort
    ){
        int pg = (page == null || page < 1) ? 1 : page;
        int sz = (size == null || size < 1) ? 10 : size;

        Sort sortSpec;
        if (sort == null || sort.isBlank() || "updated".equalsIgnoreCase(sort)) {
            // 기본: serviceDetail.apiUpdatedAt 최신순 + svcId 오름차순 보조 정렬
            sortSpec = Sort.by(
                    Sort.Order.desc("detail.apiUpdatedAt"),
                    Sort.Order.asc("svcId")
            );
        } else if ("svcId".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Order.asc("svcId"));
        } else if ("title".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Order.asc("title"));  // 가나다
        } else if ("popular".equalsIgnoreCase(sort)) {
            // 인기 순: 조회수 내림차순
            sortSpec = Sort.by(Sort.Direction.DESC, "viewCount");
        } else {
            // 알 수 없는 sort 값이면 기본으로 업데이트순 사용
            sortSpec = Sort.by(
                    Sort.Order.desc("detail.apiUpdatedAt"),
                    Sort.Order.asc("svcId")
            );
        }

        Pageable pageable = PageRequest.of(pg-1, sz, sortSpec);

        // 검색 수행 (serviceList + serviceDetail 컬럼 모두에서 검색)
        Page<Gov24ServiceEntity> pageResult =
                repo.search(
                        (keyword == null || keyword.isBlank()) ? null : keyword,
                        (category == null || category.isBlank()) ? null : category,
                        pageable
                );

        // 엔티티 → DTO 매핑
        List<ServiceSummary> dtoList = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageResponse<>(
                pg,
                sz,
                dtoList.size(),
                pageResult.getTotalElements(),
                dtoList
        );
    }

    public PageResponse<ServiceScheduleItem> listSchedule(Integer page, Integer perPage) {
        int pg = (page == null || page < 1) ? 1 : page;
        int pp = (perPage == null || perPage < 1) ? 200 : perPage;

        Pageable pageable = PageRequest.of(pg - 1, pp, Sort.by("deadline").ascending());

        var result = repo.findByDeadlineIsNotNull(pageable);

        List<ServiceScheduleItem> data = result.getContent().stream()
                .map(e -> new ServiceScheduleItem(
                        e.getSvcId(),
                        e.getTitle(),
                        e.getProviderName(),
                        e.getCategory(),
                        e.getApplyPeriod(),
                        e.getDeadline()
                ))
                .toList();

        return new PageResponse<>(
                pg,
                pp,
                data.size(),
                (int) result.getTotalElements(),
                data
        );
    }

    private ServiceSummary toDto(Gov24ServiceEntity e){
        return new ServiceSummary(
                e.getSvcId(),
                e.getTitle(),
                e.getProviderName(),
                e.getCategory(),
                e.getSummary(),
                e.getDetailUrl(),                 // detailUrl = null
                e.getApplyPeriod(),
                e.getApplyMethod(),
                e.getRegDate(),
                e.getDeadline(),
                e.getViewCount()
        );
    }

    // 조회수 증가용 (읽기 전용 해제)
    @Transactional
    public void increaseViewCount(String svcId) {
        repo.incrementViewCount(svcId);
    }
}

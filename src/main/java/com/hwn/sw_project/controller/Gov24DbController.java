package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.service.gov24.Gov24DbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gov24/db")
public class Gov24DbController {
    private final Gov24DbService dbService;

    /**
     * DB 기반 지원금 목록 조회
     * - q: 키워드 검색
     * - category: 카테고리 필터 (예: "취업", "창업/자영업"...). 전체는 null/빈문자
     * - sort: "title" 이면 가나다순, 생략 시 svcId 순
     */
    @GetMapping("/services")
    public PageResponse<ServiceSummary> listFromDb(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort
    ){
        return dbService.searchFromDb(page, size, keyword, category, sort);
    }

    // 클릭 추적 API
    @PostMapping("/services/{svcId}/click")
    public void click(@PathVariable String svcId) {
        dbService.increaseViewCount(svcId);
    }
}

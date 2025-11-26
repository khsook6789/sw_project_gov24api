package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.gov24.RecommendationItem;
import com.hwn.sw_project.dto.gov24.UserProfile;
import com.hwn.sw_project.service.match.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @PostMapping("/recommendations")
    public Mono<List<RecommendationItem>> recommend(
            @Valid @RequestBody UserProfile user,
            @RequestParam(name = "top",defaultValue = "30") int top
            ){
        int topN = Math.max(1, Math.min(top, 100));

        log.info(">>> /api/recommendations request profile = {}, top = {}", user, top);
        return recommendationService.recommend(user, topN);
    }
}

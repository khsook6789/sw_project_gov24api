package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.gov24.RecommendationItem;
import com.hwn.sw_project.dto.gov24.UserProfile;
import com.hwn.sw_project.dto.nl.NlParsedQuery;
import com.hwn.sw_project.dto.nl.NlRecommendRequest;
import com.hwn.sw_project.service.match.RecommendationService;
import com.hwn.sw_project.service.nl.NaturalLanguageQueryService;
import com.hwn.sw_project.service.nl.NlToUserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NlRecommendationController {
    private final NaturalLanguageQueryService nlService;
    private final RecommendationService recommendationService;

    @PostMapping("/recommendations/nl")
    public Mono<List<RecommendationItem>> recommendByNaturalLanguage(@RequestBody NlRecommendRequest req) {
        // 1) 자연어 → NlParsedQuery
        NlParsedQuery parsed = nlService.parse(req.query());
        log.info("NL parsed = {}", parsed);

        // 2) NlParsedQuery → UserProfile
        UserProfile profile = NlToUserProfileMapper.toUserProfile(parsed);
        log.info("UserProfile from NL = {}", profile);

        int top = (req.top() == null || req.top() < 1) ? 5 : req.top();

        // 3) 추천 로직 사용
        return recommendationService.recommendWithRanking(profile, parsed.keywords(), top);
    }
}

package com.hwn.sw_project.service.match;

import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.dto.gov24.SupportConditionsDTO;
import com.hwn.sw_project.dto.gov24.SupportConditionsPage;
import com.hwn.sw_project.dto.gov24.RecommendationItem;
import com.hwn.sw_project.dto.gov24.UserProfile;
import com.hwn.sw_project.service.gov24.Gov24Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final Gov24Client gov24Client;

    // 한 번에 받아오는 supportConditions 페이지 사이즈
    private static final int CONDITIONS_PER_PAGE = 200;

    /**
     * 사용자 프로필 기반 추천
     * @param user 사용자 입력
     * @param topN 상위 몇 개까지 반환할지
     */
    public Mono<List<RecommendationItem>> recommend(UserProfile user, int topN) {
        int internalTop = topN*20;

        return scanAllSupportConditions()
                .filter(sc -> SupportMatcher.matches(sc, user))
                .doOnSubscribe(s -> log.info("scanning supportConditions..."))
                .collectList()
                .doOnNext(list -> log.info("matched supportConditions: {}", list.size()))
                .flatMap(list -> {
                    // 1) 점수 계산 + 정렬
                    var scored = list.stream()
                            .map(sc -> new Scored(
                                    sc.서비스ID(),
                                    SupportMatcher.score(sc, user),
                                    SupportMatcher.reasons(sc, user)
                            ))
                            .filter(s->s.svcId != null && !s.svcId.isBlank())
                            .sorted((a, b) -> Double.compare(b.score, a.score))
                            .toList();

                    log.info("scored size (total): {}", scored.size());

                    // 2) svcId 기준 distinct + internalTop까지
                    var topScored = scored.stream()
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(
                                            s -> s.svcId,
                                            s -> s,
                                            (a, b) -> a
                                    ),
                                    m -> m.values().stream()
                                            .sorted((a, b) -> Double.compare(b.score, a.score))
                                            .limit(internalTop)
                                            .toList()
                            ));

                    log.info("topScored size: {}", topScored.size());

                    var svcIds = topScored.stream()
                            .map(s -> s.svcId)
                            .toList();

                    log.info("top svcIds = {}", svcIds);

                    if (svcIds.isEmpty()) {
                        return Mono.just(List.<RecommendationItem>of());
                    }

                    // 3) svcId별로 단건 조회 → (Scored + Summary) join
                    return Flux.fromIterable(topScored)
                            .concatMap(scoredItem ->
                                    gov24Client.fetchServiceSummaryBySvcId(scoredItem.svcId)
                                            .map(summary -> new Joined(scoredItem, summary))
                                            .onErrorResume(ex -> {
                                                log.warn("⚠ serviceList 단건 조회 실패: svcId={}, ex={}",
                                                        scoredItem.svcId, ex.toString());
                                                return Mono.empty();
                                            })
                            )
                            .collectList()
                            .map(joinedList -> {
                                log.info("after join (per-id, before category boost): {}", joinedList.size());

                                // 4) RecommendationItem로 변환
                                List<RecommendationItem> items = joinedList.stream()
                                        .map(j -> {
                                            var s = j.scored();
                                            var sum = j.summary();
                                            double roundScore = Math.round(s.score * 10000) / 10000.0;

                                            return new RecommendationItem(
                                                    sum.svcId(),
                                                    sum.title(),
                                                    sum.providerName(),
                                                    sum.category(),
                                                    sum.summary(),
                                                    sum.applyPeriod(),
                                                    sum.applyMethod(),
                                                    roundScore,
                                                    s.reasons
                                            );
                                        })
                                        .toList();

                                // 5) 카테고리 boost 적용
                                List<RecommendationItem> boosted =
                                        applyCategoryBoost(items, user.category());

                                // 6) 최종 topN 잘라서 리턴
                                var finalList = boosted.stream()
                                        .limit(topN)
                                        .toList();

                                log.info("after category boost & limit: {}", finalList.size());
                                return finalList;
                            });
                });
    }

    /**
     *  NL용 추천: 범위 확장, 키워드/카테고리로 재정렬
     */
    public Mono<List<RecommendationItem>> recommendWithRanking(
            UserProfile user,
            List<String> keywords,
            int requestedTop
    ){
        return recommend(user, requestedTop)
                .map(list -> {
                    log.info("before re-ranking: size={}", list.size());

                    List<RecommendationItem> ranked = list;

                    // 키워드 기반 re-ranking
                    ranked = applyKeywordBoost(ranked, keywords);

                    // 카테고리 기반 보너스 정렬
                    ranked = applyCategoryBoost(ranked, user.category());

                    var finalList = ranked.stream()
                            .limit(requestedTop)
                            .toList();

                    log.info("after re-ranking: final size={}", finalList.size());
                    return finalList;
                });
    }

    /**
     * 키워드 기반 re-ranking (결과 개수는 그대로, 순서만 변경)
     */
    private List<RecommendationItem> applyKeywordBoost(List<RecommendationItem> list, List<String> keywords) {
        if (list == null || list.isEmpty()) return list;
        if (keywords == null || keywords.isEmpty()) return list;

        var cleanedKeywords = keywords.stream()
                .filter(k -> k != null && !k.isBlank())
                .map(String::toLowerCase)
                .toList();

        if (cleanedKeywords.isEmpty()) return list;

        return list.stream()
                .sorted((a, b) -> {
                    int aHit = countKeywordHit(a, cleanedKeywords);
                    int bHit = countKeywordHit(b, cleanedKeywords);

                    if (aHit != bHit) {
                        return Integer.compare(bHit, aHit); // 키워드 더 많이 맞는 순으로
                    }
                    // 키워드 적중 수가 같으면 score 기준 내림차순 유지
                    return Double.compare(b.score(), a.score());
                })
                .toList();
    }

    private int countKeywordHit(RecommendationItem item, List<String> keywords) {
        String text = (
                safe(item.title()) + " " +
                        safe(item.summary()) + " " +
                        safe(item.applyMethod())
        ).toLowerCase();

        int count = 0;
        for (String k : keywords) {
            if (text.contains(k)) {
                count++;
            }
        }
        return count;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * 카테고리 기반 보너스 정렬 (Soft Filtering)
     * - category score +0.8 가중치
     * - 포함되는 정책이 앞으로 오도록 정렬
     */
    private List<RecommendationItem> applyCategoryBoost(List<RecommendationItem> list, String preferredCategory) {
        if (list == null || list.isEmpty()) return list;
        if (preferredCategory == null || preferredCategory.isBlank()) return list;

        String cat = preferredCategory.trim();

        // 새로운 점수 적용된 리스트 생성
        record ScoredItem(RecommendationItem item, double boostedScore) {}

        var boosted = list.stream()
                .map(item -> {
                    double score = item.score();

                    // 카테고리를 부분매칭(포함 여부 기준) 적용
                    if (item.category() != null && item.category().contains(cat)) {
                        score += 0.8; // Soft Boost!
                    }
                    return new ScoredItem(item, score);
                })
                .sorted((a, b) -> Double.compare(b.boostedScore(), a.boostedScore()))
                .map(ScoredItem::item)
                .toList();

        return boosted;
    }

    /**
     * supportConditions 전 페이지 스캔 Flux
     * - totalCount를 모르므로, currentCount가 0이 될 때 종료.
     */
    private Flux<SupportConditionsDTO> scanAllSupportConditions() {
        return Flux
                .range(1, 1000) // 안전상 상한(필요시 조정)
                .concatMap(page -> gov24Client.fetchSupportConditionsPage(page, CONDITIONS_PER_PAGE))
                .takeUntil(page -> page.currentCount() == null || page.currentCount() == 0)
                .flatMapIterable(SupportConditionsPage::data);
    }



    /**
     * supportConditions 점수 결과(Scored) + serviceList 요약(ServiceSummary) join
     */
    private List<RecommendationItem> mapJoin(List<Scored> scored,
                                             List<ServiceSummary> summaries,
                                             String categoryFilter) {
        Map<String, ServiceSummary> byId = summaries.stream()
                .collect(Collectors.toMap(ServiceSummary::svcId, s -> s, (a, b) -> a));

        List<RecommendationItem> result = new ArrayList<>();
        for (Scored s : scored) {
            var sum = byId.get(s.svcId);
            if (sum == null) continue;
            double roundScore = Math.round(s.score * 10000) / 10000.0;

            // 필요하면 카테고리 필터:
            // if (categoryFilter != null && !categoryFilter.isBlank()) {
            //     if (sum.category() == null || !sum.category().contains(categoryFilter)) {
            //         continue;
            //     }
            // }

            result.add(new RecommendationItem(
                    sum.svcId(),
                    sum.title(),
                    sum.providerName(),
                    sum.category(),
                    sum.summary(),
                    sum.applyPeriod(),
                    sum.applyMethod(),
                    roundScore,
                    s.reasons
            ));
        }
        return result;
    }

    private record Scored(String svcId, double score, List<String> reasons) {}

    private record Joined(Scored scored, ServiceSummary summary) {}
}

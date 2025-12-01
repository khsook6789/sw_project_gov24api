package com.hwn.sw_project.service.match;

import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.dto.gov24.SupportConditionsDTO;
import com.hwn.sw_project.dto.gov24.SupportConditionsPage;
import com.hwn.sw_project.dto.gov24.RecommendationItem;
import com.hwn.sw_project.dto.gov24.UserProfile;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import com.hwn.sw_project.service.gov24.Gov24Client;
import com.hwn.sw_project.service.gov24.Gov24Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final Gov24Client gov24Client;
    private final Gov24ServiceRepository serviceRepo;

    // 한 번에 받아오는 supportConditions 페이지 사이즈
    private static final int CONDITIONS_PER_PAGE = 200;

    // ★ In-memory 캐시
    private final AtomicReference<List<SupportConditionsDTO>> cachedConditions = new AtomicReference<>();
    private volatile Instant cachedAt = null;
    // TTL은 적당히 조정 가능 (예: 30분)
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 사용자 프로필 기반 추천
     * @param user 사용자 입력
     * @param topN 상위 몇 개까지 반환할지
     */
    public Mono<List<RecommendationItem>> recommend(UserProfile user, int topN) {
        int internalTop = topN*20;

        return getAllSupportConditionsCached()
                .flatMapMany(Flux::fromIterable)
                .filter(sc -> SupportMatcher.matches(sc, user))
                .doOnSubscribe(s -> log.info("scanning supportConditions..."))
                .collectList()
                .doOnNext(list -> log.info("matched supportConditions: {}", list.size()))
                .flatMap(list -> {

                    // svcId → SupportConditionsDTO 매핑 (모든 필터된 조건 저장)
                    Map<String, SupportConditionsDTO> condById = list.stream()
                            .filter(sc -> sc.서비스ID() != null && !sc.서비스ID().isBlank())
                            .collect(Collectors.toMap(
                                    SupportConditionsDTO::서비스ID,
                                    s -> s,
                                    (a, b) -> a
                            ));

                    // 1) 점수 계산 + 정렬
                    var scored = list.stream()
                            .map(sc -> new Scored(
                                    sc.서비스ID(),
                                    SupportMatcher.score(sc, user)
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

                    // 3) DB에서 serviceList 요약 한 번에 조회 (블로킹이므로 boundedElastic 사용)
                    return Mono.fromCallable(() -> serviceRepo.findBySvcIdIn(svcIds))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(entities -> {
                                // Entity → ServiceSummary 변환
                                List<ServiceSummary> summaries = entities.stream()
                                        .map(this::toSummary)
                                        .toList();

                                // Scored + Summary join
                                List<RecommendationItem> items = mapJoin(topScored, summaries, condById, user);

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

    private ServiceSummary toSummary(Gov24ServiceEntity e) {
        if (e == null) return null;
        return new ServiceSummary(
                e.getSvcId(),
                e.getTitle(),
                e.getProviderName(),
                e.getCategory(),
                e.getSummary(),
                e.getDetailUrl(),
                e.getApplyPeriod(),
                e.getApplyMethod(),
                e.getRegDate(),
                e.getDeadline(),
                e.getViewCount()
        );
    }

    /**
     * supportConditions 점수 결과(Scored) + serviceList 요약(ServiceSummary) join
     */
    private List<RecommendationItem> mapJoin(List<Scored> scored,
                                             List<ServiceSummary> summaries,
                                             Map<String, SupportConditionsDTO> condById,
                                             UserProfile user) {
        Map<String, ServiceSummary> byId = summaries.stream()
                .collect(Collectors.toMap(ServiceSummary::svcId, s -> s, (a, b) -> a));

        List<RecommendationItem> result = new ArrayList<>();
        for (Scored s : scored) {
            var sum = byId.get(s.svcId);
            if (sum == null) continue;

            var cond = condById.get(s.svcId);

            List<String> reasons = (cond != null)
                    ? SupportMatcher.reasons(cond, user)
                    : List.of();

            double roundScore = Math.round(s.score * 10000) / 10000.0;

            result.add(new RecommendationItem(
                    sum.svcId(),
                    sum.title(),
                    sum.providerName(),
                    sum.category(),
                    sum.summary(),
                    sum.applyPeriod(),
                    sum.applyMethod(),
                    roundScore,
                    reasons
            ));
        }
        return result;
    }

    private Mono<List<SupportConditionsDTO>> getAllSupportConditionsCached() {
        var current = cachedConditions.get();
        var now = Instant.now();

        if (current != null && cachedAt != null && Duration.between(cachedAt, now).compareTo(CACHE_TTL) < 0) {
            // 캐시 유효
            log.info("using cached supportConditions, size={}", current.size());
            return Mono.just(current);
        }

        // 캐시 없거나 만료 → 새로 로딩
        return scanAllSupportConditions()
                .collectList()
                .doOnNext(list -> {
                    cachedConditions.set(list);
                    cachedAt = Instant.now();
                    log.info("🔄 refreshed supportConditions cache, size={}", list.size());
                });
    }


    private record Scored(String svcId, double score) {}
}

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
                            .sorted((a, b) -> Double.compare(b.score, a.score))
                            .toList();

                    log.info("scored size (total): {}", scored.size());

                    // 2) 상위 topN만 사용 (svcId 기준으로 distinct)
                    var topScored = scored.stream()
                            .filter(s -> s.svcId != null)
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(s -> s.svcId, s -> s, (a, b) -> a),
                                    m -> m.values().stream()
                                            .sorted((a, b) -> Double.compare(b.score, a.score))
                                            .limit(topN)
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

                    // 3) 각 svcId별로 serviceSummary 개별 조회
                    return Flux.fromIterable(svcIds)
                            .concatMap(svcId ->
                                    gov24Client.fetchServiceSummaryBySvcId(svcId)  // 🔹 새로 만들 메서드
                                            .map(summary -> new AbstractMap.SimpleEntry<>(svcId, summary))
                                            .onErrorResume(ex -> {
                                                log.warn("⚠ serviceList 단건 조회 실패: svcId={}, ex={}", svcId, ex.toString());
                                                return Mono.empty(); // 이 svcId는 건너뜀
                                            })
                            )
                            .collectList()
                            .map(entries -> {
                                // svcId -> ServiceSummary 맵으로 만들기
                                Map<String, ServiceSummary> summaryMap = entries.stream()
                                        .filter(e -> e.getValue() != null)
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (a, b) -> a
                                        ));

                                // 4) 점수 + 요약 join
                                List<RecommendationItem> result = new ArrayList<>();
                                for (Scored s : topScored) {
                                    var sum = summaryMap.get(s.svcId);
                                    if (sum == null) continue;
                                    double roundScore = Math.round(s.score*10000)/10000.0;

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

                                log.info("after join (per-id): {}", result.size());
                                return result;
                            });
                });
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

    private List<RecommendationItem> mapJoin(List<Scored> scored,
                                             List<ServiceSummary> summaries,
                                             String categoryFilter) {
        Map<String, ServiceSummary> byId = summaries.stream()
                .collect(Collectors.toMap(ServiceSummary::svcId, s -> s, (a, b) -> a));

        List<RecommendationItem> result = new ArrayList<>();
        for (Scored s : scored) {
            var sum = byId.get(s.svcId);
            if (sum == null) continue;
            double roundScore = Math.round(s.score*10000)/10000.0;

//            if (categoryFilter != null && !categoryFilter.isBlank()) {
//                if (sum.category() == null || !sum.category().contains(categoryFilter)) {
//                    continue; // 카테고리 안 맞으면 제외
//                }
//            }

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
}

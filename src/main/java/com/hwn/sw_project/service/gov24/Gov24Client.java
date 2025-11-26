package com.hwn.sw_project.service.gov24;

import com.fasterxml.jackson.databind.JsonNode;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.dto.gov24.SupportConditionsPage;
import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.util.Gov24DateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Gov24Client {
    private final WebClient gov24WebClient;

    @Value("${app.gov24.api-key}")
    private String apiKey;

    /**
     * supportConditions 페이지 조회
     */
    public Mono<SupportConditionsPage> fetchSupportConditionsPage(int page, int perPage){
        return gov24WebClient.get()
                .uri(uri -> uri.path("/supportConditions")
                        .queryParam("page", page)
                        .queryParam("perPage", perPage)
                        .queryParam("returnType", "JSON")
                        .queryParam("serviceKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(SupportConditionsPage.class);
    }

    /**
     * svcId 목록을 받아 ServiceList에서 카드 정보(제목/요약/분야/신청방법 등) 가져오기
     * - gov24의 /serviceList는 cond 검색을 지원한다.
     *   예: cond[or][서비스ID::EQ]=000000465790
     */
    public Mono<ServiceSummary> fetchServiceSummaryBySvcId(String svcId){
        return gov24WebClient.get()
                .uri(uri -> uri.path("/serviceList")
                        .queryParam("page",1)
                        .queryParam("perPage",1)
                        .queryParam("serviceKey",apiKey)
                        .queryParam("cond[서비스ID::EQ]", svcId)
//                        .queryParam("cond[or][서비스ID::EQ]", svcId)
                        .queryParam("returnType", "JSON")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(root -> {
                    int current = root.path("currentCount").asInt(-1);
                    log.info("serviceList by svcId={} -> currentCount={}", svcId, current);
                })
                .map(this::toServiceSummaryPage)
                .flatMap(page -> {
                    var data = page.data();
                    if (data == null || data.isEmpty()) {
                        log.info("no ServiceSummary found for svcId={}", svcId);
                        return Mono.empty();    //  null 대신 빈 Mono
                    }
                    return Mono.just(data.get(0));
                });
    }

    public Mono<List<ServiceSummary>> fetchServiceSummariesBySvcIds(List<String> svcIds){
        if (svcIds == null || svcIds.isEmpty()) {
            return Mono.just(List.of());
        }

        return gov24WebClient.get()
                .uri(uri -> buildServiceListByIds(
                        uri,
                        svcIds,
                        1,                          // page
                        Math.max(svcIds.size(), 1)
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::toServiceSummaryPage)
                .map(PageResponse::data); // List<ServiceSummary>
    }

    private java.net.URI buildServiceListByIds(UriBuilder uri, List<String> svcIds, int page, int perPage){
        var b = uri.path("/serviceList")
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .queryParam("returnType", "JSON")
                .queryParam("serviceKey", apiKey);

        for (String id : svcIds) {
            b.queryParam("cond[서비스ID::EQ]", id);
        }
        return b.build();
    }

    private PageResponse<ServiceSummary> toServiceSummaryPage(JsonNode root){
        int page = root.path("page").asInt(1);
        int perPage = root.path("perPage").asInt(10);
        long totalCount = root.path("totalCount").asLong(0);
        int currentCount = root.path("currentCount").asInt(0);

        List<ServiceSummary> list = new ArrayList<>();
        for (JsonNode n : root.path("data")) {
            var svcId = n.path("서비스ID").asText(null);
            var title = n.path("서비스명").asText(null);
            var provider = n.path("소관기관명").asText(null);
            var category = n.path("서비스분야").asText(null);
            var summary = n.path("서비스목적요약").asText(null);
            var applyPeriod = n.path("신청기한").asText(null);
            var applyMethod = n.path("신청방법").asText(null);
            var regDateRaw = n.path("등록일시").asText(null);
            var deadlineRaw = n.path("신청기한").asText(null);

            var regDate  = Gov24DateParser.parseSingleDate(regDateRaw);
            var deadline = Gov24DateParser.parseSingleDate(deadlineRaw);

            // 상세 URL이 있다면 필드명에 맞춰 추출
            String detailUrl = null;
            if (n.has("온라인신청사이트URL")) {
                detailUrl = n.path("온라인신청사이트URL").asText(null);
            }

            list.add(new ServiceSummary(
                    svcId, title, provider, category, summary, detailUrl, applyPeriod, applyMethod, regDate, deadline, 0L
            ));
        }
        return new PageResponse<>(page, perPage, currentCount, totalCount, list);
    }
}

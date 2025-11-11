package com.hwn.sw_project.service.gov24;

import com.fasterxml.jackson.databind.JsonNode;
import com.hwn.sw_project.dto.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class Gov24Service {
    private final WebClient gov24WebClient;

    public Mono<PageResponse<ServiceSummary>> listServices(Integer page, Integer perPage){
        final int pg = (page == null || page < 1) ? 1 : page;
        final int pp = (perPage == null || perPage < 1) ? 10 : perPage;

        return gov24WebClient.get()
                .uri(uri -> uri.path("/serviceList")
                        .queryParam("page", pg)
                        .queryParam("perPage", pp)
                        .queryParam("returnType", "JSON")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::toPageResponse);

    }

    private PageResponse<ServiceSummary> toPageResponse(JsonNode n) {
        int page = n.path("page").asInt(1);
        int perPage = n.path("perPage").asInt(10);
        int currentCount = n.path("currentCount").asInt(0);
        long totalCount = n.path("totalCount").asLong(currentCount);

        List<ServiceSummary> list = new ArrayList<>();
        JsonNode data = n.path("data");
        if (data != null && data.isArray()) {
            for(JsonNode jn : data) {
                list.add(new ServiceSummary(
                        t(jn, "서비스ID"),
                        t(jn, "서비스명"),
                        t(jn, "소관기관명"),
                        t(jn, "서비스분야"),
                        t(jn, "서비스목적요약"),
                        t(jn, "상세조회URL"),
                        t(jn, "신청기한"),
                        t(jn, "신청방법")
                ));
            }
        }
        return new PageResponse<>(page, perPage, currentCount, totalCount, list);
    }

    private String t(JsonNode n, String... keys) {
        for (String k : keys){
            if(n.has(k)&&!n.get(k).isNull()){
                return n.get(k).asText();
            }
        }
        return null;
    }
}
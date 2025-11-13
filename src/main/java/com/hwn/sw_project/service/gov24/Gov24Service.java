package com.hwn.sw_project.service.gov24;

import com.fasterxml.jackson.databind.JsonNode;
import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceDetail;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class Gov24Service {
    private final WebClient gov24WebClient;

    public Mono<PageResponse<ServiceSummary>> listServices(Integer page, Integer perPage){
        final int pg = (page == null || page < 1) ? 1 : page;
        final int pp = (perPage == null || perPage < 1) ? 100 : perPage;

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

    public Mono<ServiceDetail> getServiceDetail(String svcId){
        return gov24WebClient.get()
                .uri(uri -> uri.path("/serviceDetail")
                        .queryParam("page",1)
                        .queryParam("perPage",1)
                        .queryParam("returnType", "JSON")
                        .queryParam("cond[서비스ID::EQ]", svcId)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::toServiceDetail);
    }

    private ServiceDetail toServiceDetail(JsonNode root){
        JsonNode data = root.path("data");
        JsonNode obj = data.isArray() ? (data.size() > 0 ? data.get(0) : null) : data;
        if (obj == null || obj.isMissingNode()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상세 데이터를 찾을 수 없습니다.");
        }

        // 원문 전체 보관
        Map<String, String> raw = new java.util.LinkedHashMap<>();
        obj.fieldNames().forEachRemaining(k -> raw.put(k, getText(obj, k, null)));

        return new ServiceDetail(
                getText(obj, "서비스ID", null),
                getText(obj, "서비스명", null),
                // 상세엔 "서비스목적요약" 대신 "서비스목적"이 올 수 있음 → coalesce
                coalesce(getText(obj, "서비스목적요약", null), getText(obj, "서비스목적", null)),
                getText(obj, "소관기관명", null),
                getText(obj, "서비스분야", null),                          // 없으면 null
                coalesce(getText(obj, "신청기간", null), getText(obj, "신청기한", null)),
                getText(obj, "신청방법", null),
                getText(obj, "구비서류", null),
                getText(obj, "문의처", null),
                getText(obj, "법령", null),
                getText(obj, "자치법규", null),
                getText(obj, "행정규칙", null),
                coalesce(getText(obj, "온라인신청사이트URL", null), getText(obj, "홈페이지URL", null)),
                getText(obj, "지원유형", null),
                getText(obj, "접수기관명", null),
                getText(obj, "수정일시", null),
                getText(obj, "지원대상", null),
                getText(obj, "지원내용", null),
                getText(obj, "선정기준", null),
                // 상세에는 보통 상세조회URL이 없으므로 null 허용
                getText(obj, "온라인신청사이트URL", null),
                raw
        );
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
                        t(jn, "온라인신청사이트URL"),
                        t(jn, "신청기한"),
                        t(jn, "신청방법")
                ));
            }
        }
        return new PageResponse<>(page, perPage, currentCount, totalCount, list);
    }

    private static String getText(JsonNode n, String key, String def) {
        JsonNode v = n.path(key);
        if (v.isMissingNode() || v.isNull()) return def;
        String s = v.asText();
        return (s == null || s.isBlank()) ? def : s;
    }

    @SafeVarargs
    private static <T> T coalesce(T... values) {
        for (T v : values) {
            if (v != null && (!(v instanceof String s) || !s.isBlank())) return v;
        }
        return null;
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
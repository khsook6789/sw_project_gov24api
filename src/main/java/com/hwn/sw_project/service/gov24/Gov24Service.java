package com.hwn.sw_project.service.gov24;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwn.sw_project.dto.gov24.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceDetail;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.entity.Gov24ServiceDetailEntity;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import com.hwn.sw_project.repository.Gov24ServiceDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.scheduler.Schedulers;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class Gov24Service {
    private final WebClient gov24WebClient;

    private final Gov24ServiceRepository serviceRepo;
    private final Gov24ServiceDetailRepository detailRepo;
    private final ObjectMapper objectMapper;

    // ---- ServiceDetail DTO -> 엔티티 매핑 ----
    private Gov24ServiceDetailEntity toDetailEntity(
            ServiceDetail dto,
            Gov24ServiceEntity serviceEntity
    ){
        // 수정일시 파싱 (여러 포맷 가능성을 대비해 헬퍼 메서드 사용)
        LocalDate apiUpdatedAt = parseUpdatedAt(dto.updatedAt());

//        String rawJson = null;
//        if (dto.raw() != null) {
//            try {
//                rawJson = objectMapper.writeValueAsString(dto.raw());
//            } catch (JsonProcessingException e) {
//
//            }
//        }

        return Gov24ServiceDetailEntity.builder()
                .svcId(serviceEntity.getSvcId())
                .supportTarget(dto.supportTarget())
                .supportContent(dto.supportContent())
                .selectionCriteria(dto.selectionCriteria())
                .requiredDocs(dto.requiredDocs())
                .inquiry(dto.inquiry())
                .law(dto.law())
                .localRegulation(dto.localRegulation())
                .adminRule(dto.adminRule())
                .homepage(dto.homepage())
                .supportType(dto.supportType())
                .receiveOrg(dto.receiveOrg())
                .apiUpdatedAt(apiUpdatedAt)
                .rawJson(null)
                .build();
    }

    public Page<Gov24ServiceEntity> searchSortedByUpdated(
            String keyword,
            String category,
            Pageable pageable
    ) {
        if ((keyword == null || keyword.isBlank()) && (category == null || category.isBlank())) {
            return serviceRepo.findAllOrderByUpdated(pageable);
        }
        return serviceRepo.search(keyword, category, pageable);
    }


    // ---- "수정일시" String -> LocalDateTime 변환 헬퍼 ----
    private LocalDate parseUpdatedAt(String updatedAt) {
        if (updatedAt == null || updatedAt.isBlank()) return null;
        try {
            return LocalDate.parse(updatedAt, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // 필요하면 다른 포맷도 추가
            return null;
        }

    }

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
                .map(this::toServiceDetail)
                .flatMap(dto ->
                        Mono.fromCallable(() -> {

                                    // summary 엔티티 조회
                                    var summaryOpt = serviceRepo.findById(svcId);

                                    if (summaryOpt.isPresent()) {
                                        var summaryEntity = summaryOpt.get();

                                        var existingDetailOpt = detailRepo.findById(svcId);

                                        var detailEntity = existingDetailOpt
                                                .map(ed -> {
                                                    // 기존 엔티티 업데이트
                                                    ed.setSupportTarget(dto.supportTarget());
                                                    ed.setSupportContent(dto.supportContent());
                                                    ed.setSelectionCriteria(dto.selectionCriteria());
                                                    ed.setRequiredDocs(dto.requiredDocs());
                                                    ed.setInquiry(dto.inquiry());
                                                    ed.setLaw(dto.law());
                                                    ed.setLocalRegulation(dto.localRegulation());
                                                    ed.setAdminRule(dto.adminRule());
                                                    ed.setHomepage(dto.homepage());
                                                    ed.setSupportType(dto.supportType());
                                                    ed.setReceiveOrg(dto.receiveOrg());
                                                    ed.setApiUpdatedAt(parseUpdatedAt(dto.updatedAt()));
//                                                    if (dto.raw() != null) {
//                                                        try {
//                                                            ed.setRawJson(objectMapper.writeValueAsString(dto.raw()));
//                                                        } catch (JsonProcessingException ignore) {}
//                                                    }
                                                    return ed;
                                                })
                                                .orElseGet(() -> toDetailEntity(dto, summaryEntity));
                                        if (detailEntity.getSvcId() == null) {
                                            throw new IllegalStateException("detailEntity.svcId is null! svcId=" + svcId);
                                        }

                                        detailRepo.save(detailEntity);
                                    }

                                    return dto;
                                })
                                .subscribeOn(Schedulers.boundedElastic())
                );
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
                        t(jn, "신청방법"),
                        0L
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
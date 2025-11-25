package com.hwn.sw_project.service.gov24;

import com.fasterxml.jackson.databind.JsonNode;
import com.hwn.sw_project.entity.Gov24ServiceEntity;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import com.hwn.sw_project.util.Gov24DateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class Gov24SyncService {
    private final WebClient gov24WebClient;
    private final Gov24ServiceRepository repo;

    /**
     * serviceList 전체를 돌면서 gov24_service 테이블에 upsert
     */
    @Transactional
    public void syncAllFromApi(){
        int page = 1;
        int perPage = 100;

        WebClient largeBufferClient = gov24WebClient.mutate()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
                )
                .build();

        while(true){

            final int pg = page;
            final int pp = perPage;

            JsonNode json = largeBufferClient.get()
                    .uri(uriBuilder-> uriBuilder
                                .path("/serviceList")
                                .queryParam("page",pg)
                                .queryParam("perPage",pp)
                                .queryParam("returnType","JSON")
                                .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(json == null){
                log.warn("serviceList response = null (page={})",page);
                break;
            }

            int currentCount = json.path("currentCount").asInt(0);
            if(currentCount == 0){
                log.info("end of serviceList. end at page={}",page);
                break;
            }

            JsonNode dataArray = json.path("data");
            List<Gov24ServiceEntity> batch = new ArrayList<>();

            dataArray.forEach(node->{
                String svcId = node.path("서비스ID").asText();
                String title = node.path("서비스명").asText("");
                String summary = node.path("서비스목적요약").asText("");
                String providerName = node.path("소관기관명").asText("");
                String category = node.path("서비스분야").asText("");
                String applyPeriod = node.path("신청기한").asText("");
                String applyMethod = node.path("신청방법").asText("");
                String detailUrl    = node.path("온라인신청사이트URL").asText(null);

                String regRaw       = node.path("등록일시").asText(null);
                String deadlineRaw  = node.path("신청기한").asText(null);

                LocalDate regDate   = Gov24DateParser.parseSingleDate(regRaw);
                LocalDate deadline  = Gov24DateParser.parseSingleDate(deadlineRaw);

                // 기존 데이터 있으면 가져와서 viewCount, detail 유지
                Gov24ServiceEntity e = repo.findById(svcId)
                        .orElseGet(() -> Gov24ServiceEntity.builder()
                                .svcId(svcId)
                                .viewCount(0L)   // 신규는 0으로
                                .build()
                        );

                // 공통 필드 업데이트
                e.setTitle(title);
                e.setSummary(summary);
                e.setProviderName(providerName);
                e.setCategory(category);
                e.setApplyPeriod(applyPeriod);
                e.setApplyMethod(applyMethod);
                e.setDetailUrl(detailUrl);
                e.setRegDate(regDate);
                e.setDeadline(deadline);

                batch.add(e);
            });

            repo.saveAll(batch);
//            log.info("저장 완료 {}건",batch.size());

            if(currentCount<perPage){
                break;
            }
            page++;
        }

        log.info("Gov24 serviceList 동기화 완료");
    }
}

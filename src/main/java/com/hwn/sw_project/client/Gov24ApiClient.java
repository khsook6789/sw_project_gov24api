package com.hwn.sw_project.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hwn.sw_project.dto.gov24.PublicServiceListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class Gov24ApiClient {
    private final WebClient gov24WebClient;

    public Mono<List<PublicServiceListItem>> fetchSeviceList(
            Integer page,
            Integer perPage,
            String q
    ){
        return gov24WebClient.get()
                .uri(uri ->{
                    var b = uri.path("/serviceList")
                            .queryParam("page", page != null?page:1)
                            .queryParam("perPage",perPage != null?perPage:10)
                            .queryParam("returnType","JSON");
                    if(q != null && !q.isBlank()){
                        b.queryParam("cond[searchTerm]", q);
                    }
                    return b.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::mapList);
    }

    /** JsonNode → 정규화 리스트 매핑 */
    private List<PublicServiceListItem> mapList(JsonNode root) {
        ArrayNode data = (ArrayNode) root.path("data");
        List<PublicServiceListItem> out = new ArrayList<>();
        data.forEach(n -> out.add(mapItem(n)));
        return out;
    }

    private PublicServiceListItem mapItem(JsonNode n) {
        Function<String, String> S = key -> {
            JsonNode node = n.get(key);
            return node == null || node.isNull() ? null : node.asText();
        };
        Function<String, Integer> I = key -> {
            JsonNode node = n.get(key);
            return (node == null || node.isNull() || !node.isNumber()) ? null : node.asInt();
        };

        PublicServiceListItem item = new PublicServiceListItem();
        item.setServiceId(S.apply("서비스ID"));
        item.setServiceName(S.apply("서비스명"));
        item.setPurposeSummary(S.apply("서비스목적요약"));
        item.setField(S.apply("서비스분야"));
        item.setOwnerOrgName(S.apply("소관기관명"));
        item.setOwnerOrgType(S.apply("소관기관유형"));
        item.setDetailUrl(S.apply("상세조회URL"));
        item.setApplyPeriod(S.apply("신청기한"));
        item.setApplyMethod(S.apply("신청방법"));
        item.setContact(S.apply("전화문의"));
        item.setReceiveOrg(S.apply("접수기관"));
        item.setSupportType(S.apply("지원유형"));
        item.setUpdatedAt(S.apply("수정일시"));
        item.setViewCount(I.apply("조회수"));
        return item;
    }
}

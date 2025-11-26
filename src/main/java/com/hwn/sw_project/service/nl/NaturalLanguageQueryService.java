package com.hwn.sw_project.service.nl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwn.sw_project.dto.nl.NlParsedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaturalLanguageQueryService {
    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public NlParsedQuery parse(String queryText) {
        try{
            Map<String, Object> body = Map.of(
                    "model", model,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", SYSTEM_PROMPT
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", queryText
                            )
                    )
            );

            JsonNode resp = openAiWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if(resp == null){
                throw new IllegalArgumentException("openAI Response is null");
            }

            String content = resp
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            // content: JSON 문자열 → NlParsedQuery 로 변환
            return objectMapper.readValue(content, NlParsedQuery.class);

        } catch (Exception e) {
            throw new RuntimeException("Error occured during nl parsing", e);
        }
    }

    private static final String SYSTEM_PROMPT = """
            너는 한국어 문장을 '지원금 검색 조건'으로 변환하는 도우미야.
                        출력은 반드시 JSON 형식으로만 하고, 아래 Java record 구조와 필드 이름을 정확히 지켜.
            
                        Java record:
                        public record NlParsedQuery(
                            Integer age,
                            String gender,          // "M" 또는 "F" 또는 null
                            String incomeBracket,   // "0-50","51-75","76-100","101-200","200+" 중 하나 또는 null
                            java.util.List<String> specialFlags,
                            String studentStatus,   // "초등학생","중학생","고등학생","대학생/대학원생","해당사항없음" 등
                            String employmentStatus,// "근로자/직장인","구직자/실업자" 등 또는 null
                            String industry,        // "음식점업","제조업","농업/임업/어업","정보통신업","기타업종" 등 또는 null
                            String category,        // "보육·교육","주거·자립","행정·안전","복지·일상","경력·취업" 등 또는 null
                            java.util.List<String> keywords  // 자유 텍스트 검색 키워드 리스트
                        ) {}
            
                        규칙:
                        - 사용자가 말하지 않은 정보는 추측하지 말고 null 또는 "해당사항없음"을 사용해.
                        - gender: 남자/남성/군인/형/오빠 등 -> "M", 여자/여성/임산부/어머니 등 -> "F".
                        - age: "29살","만24세","20대 초반" 같은 표현에서 가능한 합리적인 정수 하나를 선택. 애매하면 null.
                        - incomeBracket: 생활비 부족/저소득/중위소득 100% 이하 등은 대략적인 구간으로 매핑하되, 애매하면 null.
                        - specialFlags 없으면 ["해당사항없음"]으로 채워.
                        - "등록금","학비","장학금" 등은 studentStatus를 "대학생/대학원생"으로 추론해도 좋다.
                        - "청년","청년층","20대" 등은 age를 20 정도로 추론해도 된다. 확신 없으면 null.
                        - category는 문맥상 가장 어울리는 분야 하나를 선택하거나, 애매하면 null.
                        - keywords에는 사용자가 말한 핵심 단어들(예: "등록금","생활비","청년","주거비")을 넣어.
            
                        예시 1:
                        입력: "등록금 관련해서 혜택을 받고싶어"
                        출력 예:
                        {
                          "age": null,
                          "gender": null,
                          "incomeBracket": null,
                          "specialFlags": ["해당사항없음"],
                          "studentStatus": "대학생/대학원생",
                          "employmentStatus": null,
                          "industry": null,
                          "category": "보육·교육",
                          "keywords": ["등록금","학비","장학금"]
                        }
            
                        예시 2:
                        입력: "생활비가 부족한 청년을 위해 적합한 정책 추천해줘"
                        출력 예:
                        {
                          "age": 20,
                          "gender": null,
                          "incomeBracket": "51-75",
                          "specialFlags": ["해당사항없음"],
                          "studentStatus": "해당사항없음",
                          "employmentStatus": "구직자/실업자",
                          "industry": null,
                          "category": "복지·일상",
                          "keywords": ["생활비","청년"]
                        }
            
                        오직 JSON만 출력해. 다른 설명 문장은 절대 쓰지 마.
            """;
}

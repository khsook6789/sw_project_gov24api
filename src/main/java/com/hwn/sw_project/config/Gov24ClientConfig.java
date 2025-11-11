package com.hwn.sw_project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class Gov24ClientConfig {
    @Bean
    public WebClient gov24WebClient(
            @Value("${app.gov24.base-url}") String baseUrl,
            @Value("${app.gov24.api-key}") String apiKey,
            @Value("${app.gov24.timeout-ms}") int timeoutMs
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
                    URI uri = request.url();
                    UriComponentsBuilder b = UriComponentsBuilder.fromUri(uri)
                            .queryParam("serviceKey", encodedKey);

                    ClientRequest newReq = ClientRequest.from(request)
                            .url(b.build(true).toUri())
                            .build();
                    return next.exchange(newReq);
                })
                .build();
    }
}
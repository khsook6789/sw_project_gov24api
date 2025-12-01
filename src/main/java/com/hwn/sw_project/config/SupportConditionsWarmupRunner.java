package com.hwn.sw_project.config;

import com.hwn.sw_project.service.match.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportConditionsWarmupRunner implements ApplicationRunner {
    private final RecommendationService recommendationService;

    public void run(ApplicationArguments args){
        log.info("Starting async supportConditions preload...");

        recommendationService.preloadSupportConditions()
                .subscribe(
                        v -> {},
                        ex -> log.warn("async preload failed: {}", ex.toString()),
                        () -> log.info("async supportConditions preload completed.")
                );
    }
}

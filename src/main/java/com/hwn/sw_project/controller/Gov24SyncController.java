package com.hwn.sw_project.controller;

import com.hwn.sw_project.service.gov24.Gov24DetailSyncService;
import com.hwn.sw_project.service.gov24.Gov24SyncService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/gov24")
@Slf4j
public class Gov24SyncController {
    private final Gov24SyncService syncService;
    private final Gov24DetailSyncService detailSyncService;

    @PostMapping("/sync-servicelist")
    public String syncServiceList(){
        syncService.syncAllFromApi();
        return "ok";
    }

    @PostMapping("/sync-details")
    public String syncAllDetails(){
        detailSyncService.syncAllDetails();
        return "ok";
    }

    /**
     *    상세가 없는(또는 apiUpdatedAt이 null) 서비스만 동기화
     *    - 이후 운영 시, 변경분/신규분만 메우고 싶을 때 사용
     */
    @PostMapping("/details/missing")
    public String syncMissingDetails() {
        log.info("[ADMIN] serviceDetail 미보유/미갱신 대상 동기화 시작");
        detailSyncService.syncMissingDetails();
        log.info("[ADMIN] serviceDetail 미보유/미갱신 대상 동기화 완료");
        return "OK - missing details synced";
    }
}

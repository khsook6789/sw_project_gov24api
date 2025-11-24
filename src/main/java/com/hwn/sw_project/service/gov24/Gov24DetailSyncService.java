package com.hwn.sw_project.service.gov24;

import com.hwn.sw_project.entity.Gov24ServiceEntity;
import com.hwn.sw_project.repository.Gov24ServiceDetailRepository;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Gov24DetailSyncService {
    private final Gov24Service gov24Service;
    private final Gov24ServiceRepository serviceRepo;
    private final Gov24ServiceDetailRepository detailRepo;

    /**
     * 모든 서비스에 대해 /serviceDetail을 호출해서
     * gov24_service_detail 테이블을 채우는 초기 동기화
     */
    public void syncAllDetails(){
        var services = serviceRepo.findAll();
        log.info("상세 동기화 시작: 총 {}개 서비스", services.size());

        int ok = 0;
        int fail = 0;

        for (Gov24ServiceEntity s : services) {
            String svcId = s.getSvcId();

            // 이미 상세가 있고, 수정일시(apiUpdatedAt)도 채워져 있으면 스킵해도 됨
            var existing = detailRepo.findById(svcId);
            if (existing.isPresent() && existing.get().getApiUpdatedAt() != null) {
                continue;
            }

            try {
                // getServiceDetail 내부에서 이미 DB 저장까지 처리함
                gov24Service.getServiceDetail(svcId).block(); // Mono -> block

                ok++;
                if (ok % 100 == 0) {
                    log.info("동기화 진행 중: {}개 완료", ok);
                }

            } catch (Exception e) {
                fail++;
                log.warn("동기화 실패: svcId={}", svcId, e);
            }
        }

        log.info("동기화 완료: 성공={} 실패={}", ok, fail);
    }

    /**
     * 상세가 없는(또는 apiUpdatedAt이 없는) 서비스만 대상으로 동기화
     * 나중에 주기적으로 돌릴 때 적합
     */
    public void syncMissingDetails() {
        var services = serviceRepo.findAll();
        log.info("상세(미보유/미갱신) 동기화 시작: 총 {}개 서비스", services.size());

        int ok = 0;
        int skip = 0;
        int fail = 0;

        for (Gov24ServiceEntity s : services) {
            String svcId = s.getSvcId();

            var existing = detailRepo.findById(svcId);
            if (existing.isPresent() && existing.get().getApiUpdatedAt() != null) {
                skip++;
                continue;
            }

            try {
                gov24Service.getServiceDetail(svcId).block();
                ok++;
            } catch (Exception e) {
                fail++;
                log.warn("상세 동기화 실패: svcId={}", svcId, e);
            }
        }

        log.info("상세(미보유/미갱신) 동기화 완료: 성공={} 스킵={} 실패={}", ok, skip, fail);
    }
}

package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceDetail;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.service.gov24.Gov24Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gov24")
public class Gov24Controller {
    private final Gov24Service service;

    @GetMapping("/services")
    public Mono<PageResponse<ServiceSummary>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer perPage
    ){
        return service.listServices(page, perPage);
    }

    @GetMapping("/services/{svcId}")
    public Mono<ServiceDetail> detail(@PathVariable String svcId){
        return service.getServiceDetail(svcId);
    }
}

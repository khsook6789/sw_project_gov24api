package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.common.PageResponse;
import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.service.gov24.Gov24Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gov24")
public class Gov24Controller {
    private final Gov24Service service;

    @GetMapping("/services")
    public Mono<PageResponse<ServiceSummary>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer perPage,
            @RequestParam(required = false,name = "q") String keyword
    ){
        return service.listServices(page, perPage, keyword);
    }
}

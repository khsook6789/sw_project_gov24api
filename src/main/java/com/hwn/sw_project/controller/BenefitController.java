package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.benefit.*;
import com.hwn.sw_project.service.benefit.BenefitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/benefits")
public class BenefitController {
    private final BenefitService service;

    @PostMapping
    public BenefitResponse create(@Valid @RequestBody BenefitRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public BenefitResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<BenefitResponse> list(
            @RequestParam(required=false) String q,
            @RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) Long providerId,
            @PageableDefault(size=20, sort="benefitId", direction=Sort.Direction.DESC) Pageable pageable
    ){return service.list(q, categoryId, providerId, pageable);}

    @PutMapping("/{id}")
    public BenefitResponse update(@PathVariable Long id, @Valid @RequestBody BenefitRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){service.delete(id);}
}

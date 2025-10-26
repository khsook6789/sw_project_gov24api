package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.provider.*;
import com.hwn.sw_project.service.provider.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {
    private  final ProviderService service;

    @PostMapping
    public ProviderResponse create(@Valid @RequestBody ProviderRequest req){
        return service.create(req);
    }

    @GetMapping("/{id}")
    public ProviderResponse get(@PathVariable Long id){
        return service.get(id);
    }

    @GetMapping
    public Page<ProviderResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String regionCode,
            @PageableDefault(size = 20, sort = "providerId", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return service.list(q, regionCode, pageable);
    }

    @PutMapping("/{id}")
    public ProviderResponse update(@PathVariable Long id, @Valid @RequestBody ProviderRequest req){
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        service.delete(id);
    }

}

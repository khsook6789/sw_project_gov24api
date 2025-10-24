package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.category.*;
import com.hwn.sw_project.service.BenefitCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apl/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final BenefitCategoryService service;

    @PostMapping
    public CategoryResponse create(@Valid @RequestBody CategoryRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<CategoryResponse> list(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "categoryId",direction = Sort.Direction.ASC) Pageable pageable
    ){return service.list(q, pageable);}

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

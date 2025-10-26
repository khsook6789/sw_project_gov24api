package com.hwn.sw_project.service.benefitcategory;

import com.hwn.sw_project.dto.category.*;
import org.springframework.data.domain.*;


public interface BenefitCategoryService {
    CategoryResponse create(CategoryRequest req);
    CategoryResponse get(Long id);
    Page<CategoryResponse> list(String keyword, Pageable pageable);
    CategoryResponse update(Long id, CategoryRequest req);
    void delete(Long id);
}

package com.hwn.sw_project.service.benefitcategory;

import com.hwn.sw_project.dto.category.*;
import com.hwn.sw_project.entity.BenefitCategory;
import com.hwn.sw_project.repository.BenefitCategoryRepository;
import com.hwn.sw_project.repository.BenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BenefitCategoryServiceImpl implements BenefitCategoryService {
    private final BenefitCategoryRepository categoryRepo;
    private final BenefitRepository benefitRepo;

    @Override
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepo.existsByName(req.name())){
            throw new IllegalStateException("Category already exists: " + req.name());
        }
        var entity = BenefitCategory.builder()
                .name(req.name())
                .build();
        return toResp(categoryRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse get(Long id){
        var category = categoryRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Category not found: " + id));
        return toResp(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(String keyword, Pageable pageable){
        String q = (keyword == null) ? null : keyword.trim();
        if (q != null && !q.isEmpty()) {
            return categoryRepo.findByNameContainingIgnoreCase(q, pageable)
                    .map(this::toResp);
        }
        return categoryRepo.findAll(pageable)
                .map(this::toResp);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest req){
        var category = categoryRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Category not found: " + id));

        if(!category.getName().equalsIgnoreCase(req.name())
        && !categoryRepo.existsByName(req.name())){
            throw new IllegalStateException("Category already exists: " + req.name());
        }

        category.setName(req.name());
        return toResp(category);
    }

    @Override
    public void delete(Long id){
        var category = categoryRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Category not found: " + id));

        long refCount = benefitRepo.countByCategory_CategoryId(id);
        if (refCount > 0) {
            throw new IllegalStateException("Cannot delete category: referenced by " + refCount + " benefit(s).");
        }

        categoryRepo.delete(category);
    }

    private CategoryResponse toResp(BenefitCategory category) {
        return new CategoryResponse(category.getCategoryId(),category.getName());
    }
}

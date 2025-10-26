package com.hwn.sw_project.service.benefit;

import com.hwn.sw_project.dto.benefit.*;
import com.hwn.sw_project.entity.*;
import com.hwn.sw_project.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BenefitServiceImpl implements BenefitService {
    private final BenefitRepository benefitRepo;
    private final BenefitCategoryRepository categoryRepo;
    private final ProviderRepository providerRepo;

    @Override
    public BenefitResponse create(BenefitRequest req) {
        var category = categoryRepo.findById(req.categoryId()).orElseThrow();
        var provider = providerRepo.findById(req.providerId()).orElseThrow();
        var entity = Benefit.builder()
                .title(req.title())
                .category(category)
                .provider(provider)
                .validFrom(req.validFrom())
                .validTo(req.validTo())
                .status(Benefit.Status.active)
                .build();
        var saved = benefitRepo.save(entity);
        return toResp(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BenefitResponse get(Long id){
        return benefitRepo.findById(id).map(this::toResp).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BenefitResponse> list(String keyword, Long categoryId, Long providerId, Pageable pageable) {
        var page = (keyword!=null && !keyword.isBlank())
                ?benefitRepo.findByTitleContainingIgnoreCase(keyword, pageable)
                :benefitRepo.findAll(pageable);
        return page.map(this::toResp);
        // 필요하면 categoryId/providerId 필터를 Spec/QueryDsl/추가 Repo 메서드로 확장
    }

    @Override
    public BenefitResponse update(Long id, BenefitRequest req) {
        var benefit = benefitRepo.findById(id).orElseThrow();
        benefit.setTitle(req.title());
        benefit.setCategory(categoryRepo.findById(req.categoryId()).orElseThrow());
        benefit.setProvider(providerRepo.findById(req.providerId()).orElseThrow());
        benefit.setValidFrom(req.validFrom());
        benefit.setValidTo(req.validTo());
        return toResp(benefit);
    }

    @Override
    public void delete(Long id){benefitRepo.deleteById(id);}

    private BenefitResponse toResp(Benefit benefit) {
        return new BenefitResponse(
                benefit.getBenefitId(),
                benefit.getTitle(),
                benefit.getCategory().getCategoryId(),
                benefit.getCategory().getName(),
                benefit.getProvider().getProviderId(),
                benefit.getProvider().getName(),
                benefit.getValidFrom(),
                benefit.getValidTo(),
                benefit.getStatus().name()
        );
    }
}

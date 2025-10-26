package com.hwn.sw_project.service.benefit;

import com.hwn.sw_project.dto.benefit.*;
import org.springframework.data.domain.*;

public interface BenefitService {
    BenefitResponse create(BenefitRequest req);
    BenefitResponse get(Long benefitId);
    Page<BenefitResponse> list(String keyword, Long categoryId, Long providerId, Pageable pageable);
    BenefitResponse update(Long benefitId, BenefitRequest req);
    void delete(Long id);
}

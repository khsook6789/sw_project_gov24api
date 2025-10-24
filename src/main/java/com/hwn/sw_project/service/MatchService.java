package com.hwn.sw_project.service;

import com.hwn.sw_project.dto.match.*;
import org.springframework.data.domain.*;

public interface MatchService {
    MatchedBenefitResponse create(MatchCreateRequest req);
    Page<MatchedBenefitResponse> listByUser(Long userId,Pageable pageable);
    void delete(Long matchId);
}

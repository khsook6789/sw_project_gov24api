package com.hwn.sw_project.service;

import com.hwn.sw_project.dto.provider.*;
import org.springframework.data.domain.*;

public interface ProviderService {
    ProviderResponse create(ProviderRequest req);
    ProviderResponse get(Long providerId);
    Page<ProviderResponse> list(String keyword, String regionCode, Pageable pageable);
    ProviderResponse update(Long providerId, ProviderRequest req);
    void delete(Long id);
}
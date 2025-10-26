package com.hwn.sw_project.service.provider;

import com.hwn.sw_project.dto.provider.ProviderRequest;
import com.hwn.sw_project.dto.provider.ProviderResponse;
import com.hwn.sw_project.entity.Provider;
import com.hwn.sw_project.entity.Region;
import com.hwn.sw_project.repository.ProviderRepository;
import com.hwn.sw_project.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderServiceImpl implements ProviderService {
    private final ProviderRepository providerRepo;
    private final RegionRepository regionRepo;

    @Override
    public ProviderResponse create(ProviderRequest req) {
        Region region = null;
        if(req.regionCode() != null && !req.regionCode().isBlank()){
            region = regionRepo.findById(req.regionCode()).orElseThrow(()->new IllegalArgumentException("Region not found: " + req.regionCode()));
        }

        if(region != null && providerRepo.existsByNameAndRegion_RegionCode(req.name(), region.getRegionCode())){
            throw new IllegalStateException("Provider already exists in region: " + req.name());
        }

        var entity = Provider.builder()
                .name(req.name())
                .type(req.type())
                .region(region)
                .build();

        return toResp(providerRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse get(Long id){
        var provider = providerRepo.findById(id)
                .orElseThrow(()->new IllegalArgumentException("Provider not found: " + id));
        return toResp(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderResponse> list(String keyword, String regionCode, Pageable pageable){
        Page<Provider> page;

        if (regionCode != null && !regionCode.isBlank()) {
            if (keyword != null && !keyword.isBlank()) {
                page = providerRepo.findByRegion_RegionCode(regionCode, pageable)
                        .map(p -> p); // 단순 pass, 아래 map에서 DTO 변환
            } else {
                page = providerRepo.findByRegion_RegionCode(regionCode, pageable);
            }
        } else if (keyword != null && !keyword.isBlank()) {
            page = providerRepo.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            page = providerRepo.findAll(pageable);
        }
        // regionCode 있음 -> regionCode 기준으로 필터
        // keyword 있음 -> provider 검색
        // else(둘다 없음) -> 전체 목록

        return page.map(this::toResp);
    }

    @Override
    public ProviderResponse update(Long id, ProviderRequest req){
        var provider = providerRepo.findById(id).orElseThrow(()->new IllegalArgumentException("Provider not found: " + id));

        Region region = null;
        if (req.regionCode() != null && !req.regionCode().isBlank()) {
            region = regionRepo.findById(req.regionCode())
                    .orElseThrow(()->new IllegalArgumentException("Region not found: " + req.regionCode()));
        }
        if (region != null && !provider.getName().equalsIgnoreCase(req.name())) {
            if (providerRepo.existsByNameAndRegion_RegionCode(req.name(), region.getRegionCode())) {
                throw new IllegalStateException("Provider already exists in region: " + req.name());
            }
        }
        provider.setName(req.name());
        provider.setType(req.type());
        provider.setRegion(region);
        return toResp(provider);
    }

    @Override
    public void delete(Long id){
        if(!providerRepo.existsById(id)){
            throw new IllegalArgumentException("Provider not found: " + id);
        }
        providerRepo.deleteById(id);
    }

    private ProviderResponse toResp(Provider provider) {
        var region = provider.getRegion();
        return new ProviderResponse(
                provider.getProviderId(),
                provider.getName(),
                provider.getType(),
                provider.getRegion() != null ? provider.getRegion().getRegionCode() : null,
                provider.getCreatedAt(),
                provider.getUpdatedAt()
        );
    }
}

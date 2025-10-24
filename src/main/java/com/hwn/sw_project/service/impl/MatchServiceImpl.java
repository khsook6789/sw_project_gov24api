package com.hwn.sw_project.service.impl;

import com.hwn.sw_project.dto.match.*;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.Benefit;
import com.hwn.sw_project.entity.UserMatchedBenefit;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.repository.BenefitRepository;
import com.hwn.sw_project.repository.UserMatchedBenefitRepository;
import com.hwn.sw_project.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class MatchServiceImpl implements MatchService {
    private final UserMatchedBenefitRepository matchRepo;
    private final AppUserRepository userRepo;
    private final BenefitRepository benefitRepo;

    @Override
    public MatchedBenefitResponse create(MatchCreateRequest req){
        AppUser user = userRepo.findById(req.userId())
                .orElseThrow(()->new IllegalArgumentException());
        Benefit benefit = benefitRepo.findById(req.benefitId())
                .orElseThrow(()->new IllegalArgumentException());

        //중복방지
        if(matchRepo.existsByUser_UserIdAndBenefit_BenefitId(user.getUserId(),benefit.getBenefitId())){
            throw new IllegalStateException();
        }

        var entity = UserMatchedBenefit.builder()
                .user(user)
                .benefit(benefit)
                .score(req.score())
                .build();

        var saved = matchRepo.save(entity);
        return toResp(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchedBenefitResponse> listByUser(Long userId, Pageable pageable){
        if(!userRepo.existsById(userId)){
            throw new IllegalArgumentException();
        }
        return matchRepo.findByUser_UserId(userId, pageable)
                .map(this::toResp);
    }

    @Override
    public void delete(Long matchId) {
        if(!matchRepo.existsById(matchId)){
            throw new IllegalArgumentException();
        }
        matchRepo.deleteById(matchId);
    }

    private MatchedBenefitResponse toResp(UserMatchedBenefit match) {
        return new MatchedBenefitResponse(
                match.getMatchId(),
                match.getUser().getUserId(),
                match.getBenefit().getBenefitId(),
                match.getBenefit().getTitle(),
                match.getScore(),
                match.getMatchedAt()
        );
    }
}

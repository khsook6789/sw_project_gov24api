package com.hwn.sw_project.service.impl;

import com.hwn.sw_project.dto.interest.*;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserInterest;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.repository.UserInterestRepository;
import com.hwn.sw_project.service.UserInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInterestServiceImpl implements UserInterestService {
    private final AppUserRepository userRepo;
    private final UserInterestRepository interestRepo;

    @Override
    public InterestResponse add(Long userId, AddInterestRequest req){
        var user = userRepo.findById(userId)
                .orElseThrow(()->new IllegalArgumentException());

        String tag = req.tag().trim();
        if (tag.isBlank()) throw new IllegalArgumentException();

        var id = new UserInterest.UserInterestId(userId, tag);

        if(interestRepo.existsById(id)){
            throw new IllegalArgumentException();
        }

        var entity = UserInterest.builder()
                .id(id)
                .user(user)
                .build();

        var saved = interestRepo.save(entity);
        return new InterestResponse(saved.getId().getTag(), saved.getCreatedAt());
    }

    @Override
    public void remove(Long userId,String tag){
        if(tag == null || tag.trim().isEmpty()){
            throw new IllegalArgumentException();
        }
        long deleted = interestRepo.deleteByIdUserIdAndIdTag(userId, tag.trim());
        if(deleted == 0){
            throw new IllegalArgumentException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestResponse> list(Long userId){
        if(!userRepo.existsById(userId)){
            throw new IllegalArgumentException();
        }
        return interestRepo.findByIdUserId(userId).stream()
                .map(ui -> new InterestResponse(
                        ui.getId().getTag(),
                        ui.getCreatedAt() != null ? ui.getCreatedAt() : Instant.EPOCH
                )).toList();
    }
}

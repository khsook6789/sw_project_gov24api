package com.hwn.sw_project.service.userinterest;

import com.hwn.sw_project.dto.interest.*;
import com.hwn.sw_project.entity.UserInterest;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.repository.UserInterestRepository;
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
                .orElseThrow(()->new IllegalArgumentException("User not found: " + userId));

        String tag = req.tag().trim();
        if (tag.isBlank()) throw new IllegalArgumentException("Tag must not be blank");

        var id = new UserInterest.UserInterestId(userId, tag);

        if(interestRepo.existsById(id)){
            throw new IllegalArgumentException("Interest already exists for user: " + tag);
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
            throw new IllegalArgumentException("Tag must not be blank");
        }
        long deleted = interestRepo.deleteByIdUserIdAndIdTag(userId, tag.trim());
        if(deleted == 0){
            throw new IllegalArgumentException("Interest not found for user: " + tag);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterestResponse> list(Long userId){
        if(!userRepo.existsById(userId)){
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return interestRepo.findByIdUserId(userId).stream()
                .map(ui -> new InterestResponse(
                        ui.getId().getTag(),
                        ui.getCreatedAt() != null ? ui.getCreatedAt() : Instant.EPOCH
                )).toList();
    }
}

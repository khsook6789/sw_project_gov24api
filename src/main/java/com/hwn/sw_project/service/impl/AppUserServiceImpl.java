package com.hwn.sw_project.service.impl;

import com.hwn.sw_project.dto.user.*;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.service.AppUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse signUp(SignUpRequest req) {
        if(userRepo.existsByUsername(req.username())){
            throw new IllegalStateException();
        }

        var user =AppUser.builder()
                .username(req.username())
                .password(encoder.encode(req.password()))
                .build();
        var saved = userRepo.save(user);
        return new UserResponse(saved.getUserId(), saved.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long userId) {
        var user = userRepo.findById(userId).orElseThrow(()->new IllegalArgumentException());
        return new UserResponse(user.getUserId(),user.getUsername());
    }
}

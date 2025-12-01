package com.hwn.sw_project.service.appuser;

import com.hwn.sw_project.dto.user.*;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserRole;
import com.hwn.sw_project.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {
    private final AppUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse signUp(SignUpRequest req) {

        // 이메일 중복 체크
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already exists: " + req.email());
        }

        // 기본 역할은 USER
        AppUser user = AppUser.builder()
                .email(req.email())
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role(UserRole.USER)
                .build();

        AppUser saved = userRepo.save(user);

        return new UserResponse(
                saved.getUserId(),
                saved.getEmail(),
                saved.getUsername()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long userId) {
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername()
        );
    }
}

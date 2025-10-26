package com.hwn.sw_project.service.auth;

import com.hwn.sw_project.dto.auth.LoginRequest;
import com.hwn.sw_project.dto.auth.TokenResponse;
import com.hwn.sw_project.dto.user.SignUpRequest;
import com.hwn.sw_project.dto.user.UserResponse;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static com.hwn.sw_project.security.SecurityConstants.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtProvider jwt;

    @Override
    public TokenResponse register(SignUpRequest req) {
        if(userRepo.existsByUsername(req.username())){
            throw new IllegalStateException("Username already exists: ");
        }
        var user = AppUser.builder()
                .username(req.username())
                .password(encoder.encode(req.password()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepo.save(user);

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getUsername(), claims);
        var refresh = jwt.createRefreshToken(user.getUsername());
        return new TokenResponse(access, refresh);
    }

    @Override
    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        var user = userRepo.findByUsername(req.username()).orElseThrow();
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getUsername(), claims);
        var refresh = jwt.createRefreshToken(user.getUsername());
        return new TokenResponse(access, refresh);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        var jws = jwt.parse(refreshToken);
        var username = jws.getPayload().getSubject();
        var user = userRepo.findByUsername(username).orElseThrow();

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getUsername(), claims);
        var refresh = jwt.createRefreshToken(user.getUsername());
        return new TokenResponse(access, refresh);
    }

    @Override
    public UserResponse me(String username) {
        var user = userRepo.findByUsername(username).orElseThrow();
        return new UserResponse(user.getUserId(), user.getUsername());
    }
}

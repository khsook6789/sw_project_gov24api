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
        if(userRepo.existsByEmail(req.email())){
            throw new IllegalStateException("Email already exists: ");
        }
        var user = AppUser.builder()
                .email(req.email())
                .username(req.username())
                .password(encoder.encode(req.password()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepo.save(user);

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getEmail(), claims);
        var refresh = jwt.createRefreshToken(user.getEmail());
        return new TokenResponse(access, refresh);
    }

    @Override
    public TokenResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        var user = userRepo.findByEmail(req.email()).orElseThrow();
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getEmail(), claims);
        var refresh = jwt.createRefreshToken(user.getEmail());
        return new TokenResponse(access, refresh);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        var jws = jwt.parse(refreshToken);
        var subject = jws.getPayload().getSubject();
        var user = userRepo.findByEmail(subject).orElseThrow();

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        var access = jwt.createAccessToken(user.getEmail(), claims);
        var refresh = jwt.createRefreshToken(user.getEmail());
        return new TokenResponse(access, refresh);
    }

    @Override
    public UserResponse me(String email) {
        var user = userRepo.findByEmail(email).orElseThrow();
        return new UserResponse(user.getUserId(), user.getEmail(),user.getUsername());
    }
}

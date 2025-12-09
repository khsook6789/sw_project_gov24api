package com.hwn.sw_project.security.oauth2;

import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserRole;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.security.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static com.hwn.sw_project.security.SecurityConstants.CLAIM_ROLES;
import static com.hwn.sw_project.security.SecurityConstants.ROLE_USER;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final AppUserRepository userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> resp = (Map<String, Object>) attributes.get("response");

        String email = (String) resp.get("email");

        if (email == null) {
            throw new IllegalStateException("Naver email is null. Make sure 'email' scope is granted in Naver Developers.");
        }

        AppUser user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login"));

        var claims = Map.of(CLAIM_ROLES, Set.of(ROLE_USER));
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), claims);
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        String targetUrl = UriComponentsBuilder
                .fromUriString("http://210.104.76.140/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}

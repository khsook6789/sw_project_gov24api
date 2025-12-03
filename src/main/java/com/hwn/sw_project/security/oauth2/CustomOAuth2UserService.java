package com.hwn.sw_project.security.oauth2;

import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if(!"naver".equals(registrationId)) {
            return oAuth2User;
        }

        // user-name-attribute=response 로 설정했기 때문에
        // attributes 안에 root JSON이 들어있고, 거기서 response 꺼냄
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String provider = "NAVER";
        String providerId = (String) response.get("id");
        String email = (String) response.get("email");
        String nickname = (String) response.get("nickname");

        if(email == null) {
            throw new IllegalArgumentException("Naver email is null. Make sure email scope is granted.");
        }

        // provider + providerId 기준으로 유저 찾기
        AppUser user = userRepo.findByProviderAndProviderId(provider,providerId)
            .orElseGet(() -> {
                // 없으면 새로 생성
                // 최초 로그인 → 새 유저 생성 (비밀번호는 의미 없음, 랜덤 문자열로)
                AppUser newUser = AppUser.builder()
                        .email(email)
                        .username(nickname != null ? nickname : email)
                        .password(encoder.encode("SOCIAL_LOGIN"))
                        .provider(provider)
                        .providerId(providerId)
                        .role(com.hwn.sw_project.entity.UserRole.USER)
                        .build();
                return userRepo.save(newUser);
            });

        // SuccessHandler에서 다시 response map을 사용할 수 있게 response 반환
        // (Principal 타입은 여기선 딱히 중요하지 않으니 그대로 OAuth2User 유지)
        return oAuth2User;
    }
}

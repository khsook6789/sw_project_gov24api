package com.hwn.sw_project.service.favorite;

import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.entity.AppUser;
import com.hwn.sw_project.entity.UserFavorite;
import com.hwn.sw_project.repository.AppUserRepository;
import com.hwn.sw_project.repository.UserFavoriteRepository;
import com.hwn.sw_project.repository.Gov24ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {
    private final UserFavoriteRepository favoriteRepo;
    private final AppUserRepository userRepo;
    private final Gov24ServiceRepository gov24ServiceRepo;

    /**
     * SecurityContext 에서 현재 로그인한 사용자의 이메일을 꺼내고
     * 그 이메일로 AppUser 엔티티를 조회하는 헬퍼 메서드
     */
    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("인증된 사용자만 사용할 수 있는 기능입니다.");
        }

        Object principal = auth.getPrincipal();
        // 보통 username == email 로 설정되어 있음
        String email = auth.getName();

        if (email == null) {
            throw new IllegalStateException("현재 인증 정보에서 이메일을 찾을 수 없습니다.");
        }

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * 즐겨찾기 추가
     */
    public void addFavorite(String svcId) {
        AppUser user = getCurrentUser();

        // 이미 즐겨찾기 되어 있으면 무시
        if (favoriteRepo.existsByUserAndSvcId(user, svcId)) {
            return;
        }

        var fav = UserFavorite.builder()
                .user(user)
                .svcId(svcId)
                .createdAt(LocalDateTime.now())
                .build();

        favoriteRepo.save(fav);
    }

    /**
     * 즐겨찾기 해제
     */
    public void removeFavorite(String svcId) {
        AppUser user = getCurrentUser();

        favoriteRepo.findByUserAndSvcId(user, svcId)
                .ifPresent(favoriteRepo::delete);
    }

    /**
     * 내 즐겨찾기 목록 (ServiceSummary 형태로 반환)
     */
    @Transactional(readOnly = true)
    public List<ServiceSummary> listMyFavorites() {
        AppUser user = getCurrentUser();
        var favorites = favoriteRepo.findByUserOrderByCreatedAtDesc(user);

        return favorites.stream()
                .map(f -> gov24ServiceRepo.findBySvcId(f.getSvcId())
                        .map(e -> new ServiceSummary(
                                e.getSvcId(),
                                e.getTitle(),
                                e.getProviderName(),
                                e.getCategory(),
                                e.getSummary(),
                                e.getDetailUrl(),
                                e.getApplyPeriod(),
                                e.getApplyMethod(),
                                e.getRegDate(),
                                e.getDeadline(),
                                e.getViewCount()
                        ))
                        .orElse(null)
                )
                .filter(x -> x != null)
                .toList();
    }
}

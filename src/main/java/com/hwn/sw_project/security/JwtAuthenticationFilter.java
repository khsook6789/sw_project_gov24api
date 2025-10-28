package com.hwn.sw_project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CLAIM_ROLES = "roles"; // Sonar 불만 줄이기용 상수
    private static final String PREFIX_BEARER = "Bearer ";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        String token = (StringUtils.hasText(header) && header.startsWith(PREFIX_BEARER))
                ? header.substring(PREFIX_BEARER.length())
                : null;

        try {
            if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                var jws = jwtProvider.parse(token);
                var claims = jws.getPayload();
                String username = claims.getSubject();

                // 1) roles 클레임을 "무조건" 컬렉션<String>으로 변환
                List<String> roleNames = extractRoleNamesSafely(claims.get(CLAIM_ROLES));

                // 2) 빈 경우에는 기본 ROLE_USER 한 개라도 부여(정책에 맞게 조정 가능)
                if (roleNames.isEmpty()) {
                    roleNames = List.of("ROLE_USER");
                }

                // 3) GrantedAuthority 리스트로 변환
                List<GrantedAuthority> authorities = roleNames.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new) // SimpleGrantedAuthority는 GrantedAuthority 구현체
                        .collect(Collectors.toList());

                // 4) Authentication 세팅
                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // (선택) UserDetails를 꼭 써야 한다면 아래처럼 교체
                // var userDetails = userDetailsService.loadUserByUsername(username);
                // Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                // SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ignored) {
            // 토큰 문제는 익명으로 흘려보내고, 보호 리소스는 Security가 401/403 판단
        }

        chain.doFilter(req, res);
    }

    /**
     * roles에 들어오는 다양한 형태(List, 배열, 문자열 등)를 안전하게 List<String>으로 변환
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRoleNamesSafely(Object raw) {
        if (raw == null) return Collections.emptyList();

        // 1) 이미 List<?> 인 경우
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) out.add(String.valueOf(o));
            }
            return out;
        }

        // 2) 배열인 경우
        if (raw.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(raw);
            List<String> out = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object v = java.lang.reflect.Array.get(raw, i);
                if (v != null) out.add(String.valueOf(v));
            }
            return out;
        }

        // 3) 콤마로 이어진 문자열인 경우 "ROLE_USER,ROLE_ADMIN"
        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) return Collections.emptyList();
        if (s.contains(",")) {
            return Arrays.stream(s.split(","))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .collect(Collectors.toList());
        }
        // 4) 단일 문자열
        return List.of(s);
    }
}

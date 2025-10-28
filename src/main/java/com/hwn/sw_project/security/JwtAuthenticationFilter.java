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

    private static final String CLAIM_ROLES = "roles";
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

                List<String> roleNames = extractRoleNamesSafely(claims.get(CLAIM_ROLES));

                if (roleNames.isEmpty()) {
                    roleNames = List.of("ROLE_USER");
                }

                List<GrantedAuthority> authorities = roleNames.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new) //GrantedAuthority 구현체
                        .collect(Collectors.toList());

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        } catch (Exception ignored) {
            // Security가 401/403 판단
        }

        chain.doFilter(req, res);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoleNamesSafely(Object raw) {
        if (raw == null) return Collections.emptyList();

        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) out.add(String.valueOf(o));
            }
            return out;
        }

        if (raw.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(raw);
            List<String> out = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                Object v = java.lang.reflect.Array.get(raw, i);
                if (v != null) out.add(String.valueOf(v));
            }
            return out;
        }

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

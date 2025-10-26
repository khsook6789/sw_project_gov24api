package com.hwn.sw_project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        String token = null;
        if(StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        try {
            if(StringUtils.hasText(token)) {
                var jws = jwtProvider.parse(token);
                var username = jws.getPayload().getSubject();

                var userDetails = userDetailsService.loadUserByUsername(username);
                var authorities = userDetails.getAuthorities();

                if(authorities == null || authorities.isEmpty()) {
                    authorities = List.of(new SimpleGrantedAuthority("Role_USER"));
                }

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails,null,authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }catch (Exception ignored) {
            // 토큰 오류/만료는 무시하고 익명으로 진행 → 보호된 리소스면 401/403은 시큐리티가 판단
        }

        chain.doFilter(req,res);
    }
}

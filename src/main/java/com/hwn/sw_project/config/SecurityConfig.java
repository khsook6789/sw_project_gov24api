package com.hwn.sw_project.config;

import com.hwn.sw_project.security.CustomUserDetailsService;
import com.hwn.sw_project.security.JwtAuthenticationFilter;
import com.hwn.sw_project.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        var jwtFilter = new JwtAuthenticationFilter(jwtProvider,userDetailsService);
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                // ✅ 1) Preflight 전역 허용
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // ✅ 2) 프론트 헬스체크용 엔드포인트 공개 (React에서 /api/health 호출)
                                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()

                                .requestMatchers("/api/gov24/**").permitAll()
                                // 로그인/회원가입/리프레시 공개
                                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()

                                // (권장) 정적 리소스/에러 경로 허용
                                .requestMatchers(
                                        "/", "/index.html", "/static/**", "/assets/**",
                                        "/favicon.ico", "/error"
                                ).permitAll()

                                // 나머지 API는 인증 필요
                                .anyRequest().authenticated()
//                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
//                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(c -> c.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception{
        return cfg.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000")); // React dev 서버
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);

//        cfg.setAllowedOrigins(List.of("http://localhost:3000")); // 도메인 나중에 수정
//        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true);

        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}

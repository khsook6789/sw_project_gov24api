package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.auth.LoginRequest;
import com.hwn.sw_project.dto.auth.TokenResponse;
import com.hwn.sw_project.dto.user.SignUpRequest;
import com.hwn.sw_project.dto.user.UserResponse;
import com.hwn.sw_project.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse>register(@RequestBody @Valid SignUpRequest req){
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse>login(@RequestBody @Valid LoginRequest req){
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse>refresh(@RequestParam("refreshToken") String refreshToken){
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse>me(Authentication authentication ){
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }
}

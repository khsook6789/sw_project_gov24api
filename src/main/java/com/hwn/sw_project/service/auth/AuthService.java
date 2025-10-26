package com.hwn.sw_project.service.auth;

import com.hwn.sw_project.dto.auth.LoginRequest;
import com.hwn.sw_project.dto.auth.TokenResponse;
import com.hwn.sw_project.dto.user.SignUpRequest;
import com.hwn.sw_project.dto.user.UserResponse;


public interface AuthService {
    TokenResponse register(SignUpRequest req);
    TokenResponse login(LoginRequest req);
    TokenResponse refresh(String refreshToken);
    UserResponse me(String username);
}

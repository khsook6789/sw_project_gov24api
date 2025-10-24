package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.user.*;
import com.hwn.sw_project.service.AppUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AppUserService userService;

    //회원가입
    @PostMapping
    public UserResponse signUp(@Valid @RequestBody SignUpRequest req){
        return userService.signUp(req);
    }

    //조회 (테스트용)
    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        return userService.get(userId);
    }
}

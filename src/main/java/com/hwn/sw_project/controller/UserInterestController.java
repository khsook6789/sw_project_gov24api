package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.interest.*;
import com.hwn.sw_project.service.userinterest.UserInterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/interests")
@RequiredArgsConstructor
public class UserInterestController {
    private final UserInterestService interestService;

    // 관심사 추가
    @PostMapping
    public InterestResponse add(@PathVariable Long userId, @Valid @RequestBody AddInterestRequest req) {
        return interestService.add(userId, req);
    }

    // 관심사 목록
    @GetMapping
    public List<InterestResponse> list(@PathVariable Long userId) {
        return interestService.list(userId);
    }

    //삭제
    @DeleteMapping("/{tag}")
    public void delete(@PathVariable Long userId,@PathVariable String tag) {
        interestService.remove(userId, tag);
    }
}

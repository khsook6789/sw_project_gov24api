package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.match.*;
import com.hwn.sw_project.service.match.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @PostMapping
    public MatchedBenefitResponse create(@Valid @RequestBody MatchCreateRequest req){
        return matchService.create(req);
    }

    @GetMapping("/users/{userId}")
    public Page<MatchedBenefitResponse> listByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20,sort = "matchedAt",direction = Sort.Direction.DESC) Pageable pageable){
        return matchService.listByUser(userId, pageable);
    }

    @DeleteMapping("/{matchId}")
    public void delete(@PathVariable Long matchId){
        matchService.delete(matchId);
    }

}

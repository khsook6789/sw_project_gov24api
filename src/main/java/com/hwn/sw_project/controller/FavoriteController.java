package com.hwn.sw_project.controller;

import com.hwn.sw_project.dto.gov24.ServiceSummary;
import com.hwn.sw_project.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    /**
     * 내 즐겨찾기 목록 조회
     * Response: ServiceSummary 리스트
     */
    @GetMapping
    public List<ServiceSummary> listMyFavorites() {
        return favoriteService.listMyFavorites();
    }

    /**
     * 즐겨찾기 추가
     * POST /api/favorites/{svcId}
     */
    @PostMapping("/{svcId}")
    public ResponseEntity<Void> addFavorite(@PathVariable String svcId){
        favoriteService.addFavorite(svcId);
        // body 없음, 201 Created 정도만 리턴
        return ResponseEntity.created(URI.create("/api/favorites/" + svcId)).build();
    }

    /**
     * 즐겨찾기 해제
     * DELETE /api/favorites/{svcId}
     */
    @DeleteMapping("/{svcId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable String svcId) {
        favoriteService.removeFavorite(svcId);
        return ResponseEntity.noContent().build();
    }
}

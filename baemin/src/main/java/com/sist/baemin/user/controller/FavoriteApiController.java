package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.dto.FavoriteRequestDto;
import com.sist.baemin.user.dto.FavoriteResponseDto;
import com.sist.baemin.user.dto.FavoriteStatusDto;
import com.sist.baemin.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FavoriteApiController {
    
    private final FavoriteService favoriteService;
    
    // 찜 목록 조회 API
    @GetMapping
    public ResponseEntity<ResultDto<FavoriteResponseDto>> getFavorites(
            @RequestParam(defaultValue = "store") String type,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자는 빈 목록 반환
            FavoriteResponseDto emptyFavorites = new FavoriteResponseDto(java.util.Collections.emptyList());
            return ResponseEntity.ok(new ResultDto<>(200, "찜 목록 조회 성공", emptyFavorites));
        }
        
        Long userId = userDetails.getUserId();
        FavoriteResponseDto favorites = favoriteService.getFavorites(userId, type);
        return ResponseEntity.ok(new ResultDto<>(200, "찜 목록 조회 성공", favorites));
    }
    
    @PostMapping
    public ResponseEntity<ResultDto<Map<String, Object>>> addFavorite(
            @RequestBody FavoriteRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자인 경우 401 반환
            return ResponseEntity.status(401).build();
        }
        
        Long userId = userDetails.getUserId();
        FavoriteStatusDto favorite = favoriteService.addFavorite(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("favoriteId", favorite.getFavoriteId());
        response.put("isFavorite", favorite.getIsFavorite());
        
        return ResponseEntity.ok(new ResultDto<>(201, "찜 목록에 추가되었습니다.", response));
    }
    
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ResultDto<Void>> deleteFavorite(
            @PathVariable Long favoriteId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자인 경우 401 반환
            return ResponseEntity.status(401).build();
        }
        
        Long userId = userDetails.getUserId();
        favoriteService.deleteFavorite(userId, favoriteId);
        
        return ResponseEntity.ok(new ResultDto<>(200, "찜 목록에서 삭제되었습니다.", null));
    }
    
    @GetMapping("/check")
    public ResponseEntity<ResultDto<FavoriteStatusDto>> checkFavoriteStatus(
            @RequestParam String type,
            @RequestParam Long targetId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자인 경우
            FavoriteStatusDto status = favoriteService.checkFavoriteStatusForAnonymous(type, targetId);
            return ResponseEntity.ok(new ResultDto<>(200, "찜 상태 확인 성공", status));
        }
        
        Long userId = userDetails.getUserId();
        FavoriteStatusDto status = favoriteService.checkFavoriteStatus(userId, type, targetId);
        return ResponseEntity.ok(new ResultDto<>(200, "찜 상태 확인 성공", status));
    }
}
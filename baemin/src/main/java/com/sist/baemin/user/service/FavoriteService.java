package com.sist.baemin.user.service;

import com.sist.baemin.user.dto.FavoriteRequestDto;
import com.sist.baemin.user.dto.FavoriteResponseDto;
import com.sist.baemin.user.dto.FavoriteStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final WishlistDbService wishlistDbService;
    private final FavoriteResponseService favoriteResponseService;
    
    public FavoriteResponseDto getFavorites(Long userId, String type) {
        return favoriteResponseService.getFavoritesResponse(userId, type);
    }
    
    public FavoriteStatusDto addFavorite(Long userId, FavoriteRequestDto request) {
        Long wishlistId = wishlistDbService.addToWishlist(userId, request);
        return favoriteResponseService.addFavoriteResponse(wishlistId);
    }
    
    public void deleteFavorite(Long userId, Long favoriteId) {
        wishlistDbService.removeFromWishlist(userId, favoriteId);
    }
    
    public FavoriteStatusDto checkFavoriteStatus(Long userId, String type, Long targetId) {
        return favoriteResponseService.getFavoriteStatusResponse(userId, type, targetId);
    }
    
    // 로그인하지 않은 사용자의 경우를 위한 메서드
    public FavoriteStatusDto checkFavoriteStatusForAnonymous(String type, Long targetId) {
        // 로그인하지 않은 사용자는 찜 상태가 항상 false
        return new FavoriteStatusDto(false, null);
    }
    
    // 로그인하지 않은 사용자의 경우를 위한 메서드
    public FavoriteStatusDto addFavoriteForAnonymous(FavoriteRequestDto request) {
        // 로그인하지 않은 사용자는 찜 추가가 불가능하므로 false 반환
        return new FavoriteStatusDto(false, null);
    }
    
    // 로그인하지 않은 사용자의 경우를 위한 메서드
    public void deleteFavoriteForAnonymous(Long favoriteId) {
        // 로그인하지 않은 사용자는 찜 삭제가 불가능하므로 아무것도 하지 않음
    }
}
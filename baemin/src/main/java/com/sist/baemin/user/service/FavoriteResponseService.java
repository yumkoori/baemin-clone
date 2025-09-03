package com.sist.baemin.user.service;

import com.sist.baemin.review.repository.ReviewRepository;
import com.sist.baemin.store.repository.StoreRepository;
import com.sist.baemin.user.domain.WishlistsEntity;
import com.sist.baemin.user.dto.FavoriteResponseDto;
import com.sist.baemin.user.dto.FavoriteStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FavoriteResponseService {
    
    private final WishlistDbService wishlistDbService;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    
    /**
     * 찜 목록 응답 변환
     */
    public FavoriteResponseDto getFavoritesResponse(Long userId, String type) {
        List<WishlistsEntity> wishlists = wishlistDbService.getWishlistsByUser(userId, type);
        
        List<FavoriteResponseDto.FavoriteItem> items = wishlists.stream()
            .map(wishlist -> {
                if (wishlist.getStore() != null) {
                    // 실제 리뷰 평점과 리뷰 수 가져오기
                    Double averageRating = reviewRepository.getAverageRatingByStoreId(wishlist.getStore().getStoreId());
                    long reviewCount = reviewRepository.countByStoreId(wishlist.getStore().getStoreId());
                    
                    // 평점이 null이면 0.0으로 설정
                    if (averageRating == null) {
                        averageRating = 0.0;
                    }
                    
                    return new FavoriteResponseDto.FavoriteItem(
                        wishlist.getWishlistId(),
                        "store",
                        wishlist.getStore().getStoreId(),
                        wishlist.getStore().getStoreName(),
                        "https://image.baemin.com/store" + wishlist.getStore().getStoreId() + ".jpg",
                        averageRating, // 실제 리뷰 평점
                        (int) reviewCount, // 실제 리뷰 개수
                        "25-40분", // 실제로는 배달 시간을 계산해야 함
                        wishlist.getStore().getDeliveryFee(), // 실제 배달비
                        wishlist.getStore().getMinimumPrice(), // 최소 주문 금액
                        wishlist.getCreatedAt()
                    );
                } else {
                    // store가 null인 경우 (데이터 오류)
                    return new FavoriteResponseDto.FavoriteItem(
                        wishlist.getWishlistId(),
                        "store",
                        0L,
                        "알 수 없는 가게",
                        "https://image.baemin.com/default.jpg",
                        0.0,
                        0,
                        "",
                        0, // deliveryFee
                        0, // minimumPrice
                        wishlist.getCreatedAt()
                    );
                }
            })
            .collect(Collectors.toList());
        
        return new FavoriteResponseDto(items);
    }
    
    /**
     * 찜 상태 확인 응답 변환
     */
    public FavoriteStatusDto getFavoriteStatusResponse(Long userId, String type, Long targetId) {
        Optional<WishlistsEntity> wishlist = wishlistDbService.checkWishlistStatus(userId, type, targetId);
        
        if (wishlist.isPresent()) {
            return new FavoriteStatusDto(true, wishlist.get().getWishlistId());
        } else {
            return new FavoriteStatusDto(false, null);
        }
    }
    
    /**
     * 찜 추가 응답 변환
     */
    public FavoriteStatusDto addFavoriteResponse(Long wishlistId) {
        return new FavoriteStatusDto(true, wishlistId);
    }
}
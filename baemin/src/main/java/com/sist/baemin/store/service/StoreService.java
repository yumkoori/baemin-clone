package com.sist.baemin.store.service;

import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.store.dto.StoreResponseDto;
import com.sist.baemin.store.repository.StoreRepository;
import com.sist.baemin.review.service.ReviewService;
import com.sist.baemin.review.dto.ReviewStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final ReviewService reviewService;
    
    // 가게 상세 조회
    public StoreResponseDto getStoreDetail(Long storeId) {
        StoreEntity store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
        
        return convertToStoreResponseDto(store);
    }
    
    // 모든 가게 목록 조회
    public List<StoreResponseDto> getAllStores() {
        List<StoreEntity> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToStoreResponseDto)
                .collect(Collectors.toList());
    }
    
    // 가게명으로 검색
    public List<StoreResponseDto> searchStoresByName(String storeName) {
        List<StoreEntity> stores = storeRepository.findByStoreNameContaining(storeName);
        return stores.stream()
                .map(this::convertToStoreResponseDto)
                .collect(Collectors.toList());
    }
    
    // 최소 주문 금액으로 필터링
    public List<StoreResponseDto> getStoresByMinimumPrice(Integer minimumPrice) {
        List<StoreEntity> stores = storeRepository.findByMinimumPriceLessThanEqual(minimumPrice);
        return stores.stream()
                .map(this::convertToStoreResponseDto)
                .collect(Collectors.toList());
    }
    
    // 배달료로 필터링
    public List<StoreResponseDto> getStoresByDeliveryFee(Integer deliveryFee) {
        List<StoreEntity> stores = storeRepository.findByDeliveryFeeLessThanEqual(deliveryFee);
        return stores.stream()
                .map(this::convertToStoreResponseDto)
                .collect(Collectors.toList());
    }
    
    // 가게 Entity 조회
    public StoreEntity getStoreEntityById(Long storeId) {
        return storeRepository.findByStoreId(storeId).orElse(null);
    }
    
    // Entity를 DTO로 변환
    private StoreResponseDto convertToStoreResponseDto(StoreEntity store) {
        // 리뷰 통계 조회 (예외 처리 포함)
        ReviewStatsDto reviewStats = null;
        try {
            reviewStats = reviewService.getReviewStatsByStore(store.getStoreId());
        } catch (Exception e) {
            // 리뷰 조회 실패 시 기본값 사용
            reviewStats = ReviewStatsDto.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }
        
        return StoreResponseDto.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .phoneNumber(store.getPhoneNumber())
                .storeAddress(store.getStoreAdress()) // Entity의 필드명이 storeAdress로 되어있음
                .minimumPrice(store.getMinimumPrice())
                .openAt(store.getOpenAt())
                .closeAt(store.getCloseAt())
                .mainImage(store.getMainImage())
                .deliveryFee(store.getDeliveryFee())
                .registerAt(store.getRegisterAt())
                .rating(reviewStats.getAverageRating())
                .reviewCount(reviewStats.getTotalReviews())
                .estimatedDeliveryTime("12~27분") // 임시 배달 시간
                .originalDeliveryFee(1900) // 임시 원래 배달료
                .build();
    }
} 
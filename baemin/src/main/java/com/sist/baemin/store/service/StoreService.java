package com.sist.baemin.store.service;

import com.sist.baemin.direction.service.DirectionsService;
import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.store.dto.StoreResponseDto;
import com.sist.baemin.store.repository.StoreRepository;
import com.sist.baemin.review.service.ReviewService;
import com.sist.baemin.review.dto.ReviewStatsDto;
import com.sist.baemin.user.domain.UserAddressEntity;
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
    private final DirectionsService directionsService; // DirectionsService 주입
    
    // 가게 상세 조회 (사용자 주소 기반)
    public StoreResponseDto getStoreDetail(Long storeId, UserAddressEntity userAddress) {
        StoreEntity store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
        
        return convertToStoreResponseDto(store, userAddress);
    }
    
    // 가게 상세 조회 (기본 사용자 주소 또는 하드코딩된 주소 사용)
    public StoreResponseDto getStoreDetail(Long storeId) {
        StoreEntity store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("가게를 찾을 수 없습니다."));
        
        // 로그인하지 않은 경우, userAddress를 null로 전달
        // 실제 애플리케이션에서는 로그인한 사용자의 주소를 가져와야 합니다.
        return convertToStoreResponseDto(store, null);
    }
    
    // 모든 가게 목록 조회
    public List<StoreResponseDto> getAllStores() {
        List<StoreEntity> stores = storeRepository.findAll();
        return stores.stream()
                .map(store -> convertToStoreResponseDto(store, null)) // 목록 조회에서는 예상 시간 계산을 생략할 수 있습니다.
                .collect(Collectors.toList());
    }
    
    // 가게명으로 검색
    public List<StoreResponseDto> searchStoresByName(String storeName) {
        List<StoreEntity> stores = storeRepository.findByStoreNameContaining(storeName);
        return stores.stream()
                .map(store -> convertToStoreResponseDto(store, null)) // 검색 결과에서는 예상 시간 계산을 생략할 수 있습니다.
                .collect(Collectors.toList());
    }
    
    // 최소 주문 금액으로 필터링
    public List<StoreResponseDto> getStoresByMinimumPrice(Integer minimumPrice) {
        List<StoreEntity> stores = storeRepository.findByMinimumPriceLessThanEqual(minimumPrice);
        return stores.stream()
                .map(store -> convertToStoreResponseDto(store, null)) // 필터링 결과에서는 예상 시간 계산을 생략할 수 있습니다.
                .collect(Collectors.toList());
    }
    
    // 배달료로 필터링
    public List<StoreResponseDto> getStoresByDeliveryFee(Integer deliveryFee) {
        List<StoreEntity> stores = storeRepository.findByDeliveryFeeLessThanEqual(deliveryFee);
        return stores.stream()
                .map(store -> convertToStoreResponseDto(store, null)) // 필터링 결과에서는 예상 시간 계산을 생략할 수 있습니다.
                .collect(Collectors.toList());
    }
    
    // 가게 Entity 조회
    public StoreEntity getStoreEntityById(Long storeId) {
        return storeRepository.findByStoreId(storeId).orElse(null);
    }
    
    // Entity를 DTO로 변환 (사용자 주소 기반)
    private StoreResponseDto convertToStoreResponseDto(StoreEntity store, UserAddressEntity userAddress) {
        System.out.println("=== StoreService.convertToStoreResponseDto() called ===");
        System.out.println("Store ID: " + store.getStoreId());
        System.out.println("Store lat: " + store.getLatitude() + ", lon: " + store.getLongitude());
        System.out.println("User address: " + userAddress);
        if (userAddress != null) {
            System.out.println("User address lat: " + userAddress.getLatitude() + ", lon: " + userAddress.getLongitude());
        }
        
        // 리뷰 통계 조회 (예외 처리 포함)
        ReviewStatsDto reviewStats = null;
        try {
            reviewStats = reviewService.getReviewStatsByStore(store.getStoreId());
        } catch (Exception e) {
            // 리뷰 조회 실패 시 기본값 사용
            System.out.println("Error getting review stats: " + e.getMessage());
            reviewStats = ReviewStatsDto.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }
        System.out.println("Review stats: " + reviewStats);
        
        // 예상 소요 시간 계산
        String estimatedDeliveryTime = "로그인 후 확인 가능";
        // userAddress가 null이 아니고, 가게와 사용자 주소의 위도/경도가 모두 존재할 경우에만 API를 호출합니다.
        if (userAddress != null && store.getLatitude() != null && store.getLongitude() != null && userAddress.getLatitude() != null && userAddress.getLongitude() != null) {
            // 가게 좌표
            String storeCoords = store.getLongitude().toString() + "," + store.getLatitude().toString();
            // 사용자 주소 좌표
            String userCoords = userAddress.getLongitude().toString() + "," + userAddress.getLatitude().toString();
            
            System.out.println("Calling DirectionsService with userCoords: " + userCoords + ", storeCoords: " + storeCoords);
            
            try {
                estimatedDeliveryTime = directionsService.getDuration(userCoords, storeCoords);
                System.out.println("DirectionsService returned: " + estimatedDeliveryTime);
            } catch (Exception e) {
                // API 호출 실패 시 기본값 사용
                System.out.println("Error calling DirectionsService: " + e.getMessage());
                estimatedDeliveryTime = "정보 없음";
            }
        } else {
            System.out.println("Not calling DirectionsService - missing coordinates or user not logged in");
            if (userAddress == null) {
                System.out.println("User address is null");
            }
            if (store.getLatitude() == null || store.getLongitude() == null) {
                System.out.println("Store coordinates are missing");
            }
            if (userAddress != null && (userAddress.getLatitude() == null || userAddress.getLongitude() == null)) {
                System.out.println("User address coordinates are missing");
            }
        }
        
        StoreResponseDto dto = StoreResponseDto.builder()
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
                .estimatedDeliveryTime(estimatedDeliveryTime) // 동적으로 계산된 배달 시간 또는 안내 메시지
                .originalDeliveryFee(1900) // 임시 원래 배달료
                .build();
        
        System.out.println("Created StoreResponseDto with estimatedDeliveryTime: " + dto.getEstimatedDeliveryTime());
        System.out.println("=== StoreService.convertToStoreResponseDto() completed ===");
        
        return dto;
    }
} 
package com.sist.baemin.store.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponseDto {
    
    private Long storeId;
    private String storeName;
    private String phoneNumber;
    private String storeAddress;
    private Integer minimumPrice;
    private LocalDateTime openAt;
    private LocalDateTime closeAt;
    private String mainImage;
    private Integer deliveryFee;
    private LocalDateTime registerAt;
    
    // 추가 필드들 (평점, 리뷰 수 등)
    private Double rating;
    private Integer reviewCount;
    private String estimatedDeliveryTime;
    private Integer originalDeliveryFee;
} 
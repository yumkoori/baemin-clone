package com.sist.baemin.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sist.baemin.user.domain.CouponEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Redis에 JSON으로 저장될 쿠폰 정보 DTO
 * Serializable 구현으로 직렬화 가능
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCacheDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long couponId;
    private String couponName;
    private String couponCode;
    private String description;
    private Integer discountAmount;
    private Integer minOrderAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    
    /**
     * Entity를 DTO로 변환
     */
    public static CouponCacheDto fromEntity(CouponEntity entity) {
        return CouponCacheDto.builder()
                .couponId(entity.getCouponId())
                .couponName(entity.getCouponName())
                .couponCode(entity.getCouponCode())
                .description(entity.getDescription())
                .discountAmount(entity.getDiscountAmount())
                .minOrderAmount(entity.getMinOrderAmount())
                .expiryDate(entity.getExpiryDate())
                .build();
    }
}


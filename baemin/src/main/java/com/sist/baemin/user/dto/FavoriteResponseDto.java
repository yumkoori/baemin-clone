package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseDto {
    
    private List<FavoriteItem> favorites;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteItem {
        private Long favoriteId;
        private String type;
        private Long storeId;
        private String storeName;
        private String storeImage;
        private Double rating;
        private Integer reviewCount;
        private String deliveryTime;
        private Integer deliveryFee;
        private Integer minimumPrice;
        private LocalDateTime addedAt;
    }
    
}
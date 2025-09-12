package com.sist.baemin.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDataDto {
    private String cartId;
    private Long storeId;
    private String storeName;
    private Long totalAmount;
    private Long deliveryFee;
    private Long discountAmount;
    private Long finalAmount;
    private Long minOrderAmount;
    private Boolean isOrderable;
    private List<CartItemDto> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Long cartItemId;
        private Long menuId;
        private String menuName;
        private Integer basePrice;
        private Integer quantity;
        private Integer totalPrice;
        private List<CartOptionDto> options;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartOptionDto {
        private Long optionId;
        private Long menuOptionValueId;
        private String optionName;
        private String optionValue;
        private Integer additionalPrice;
    }
}
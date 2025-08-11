package com.sist.baemin.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    
    private String cartId;
    private Long storeId;
    private String storeName;
    private List<CartItemDto> items;
    private Integer totalAmount;
    private Integer deliveryFee;
    private Integer discountAmount;
    private Integer finalAmount;
    private Integer minOrderAmount;
    private Boolean isOrderable;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Long cartItemId;
        private Long menuId;
        private String menuName;
        private Integer basePrice;
        private Integer quantity;
        private List<OptionDto> options;
        private Integer totalPrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Long optionId;
        private String optionName;
        private String optionValue;
        private Integer additionalPrice;
        private Long menuOptionValueId; // MenuOptionValue ID 추가
    }
}
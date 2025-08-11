package com.sist.baemin.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartAddRequestDto {
    
    private Long storeId;
    private Long menuId;
    private Integer quantity;
    private List<OptionRequest> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionRequest {
        private Long optionId;           // MenuOption의 ID
        private Long menuOptionValueId;  // MenuOptionValue의 ID (직접 전달)
    }
}
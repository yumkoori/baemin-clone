package com.sist.baemin.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDto {
    private List<CartItemDetailDto> items;
    
    public CartResponseDto() {}
    
    public CartResponseDto(List<CartItemDetailDto> items) {
        this.items = items;
    }
}

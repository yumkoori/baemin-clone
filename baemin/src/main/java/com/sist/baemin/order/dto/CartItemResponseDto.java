package com.sist.baemin.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponseDto {
    private Long cartItemId;
    private Integer totalItems;
    private Integer totalAmount;
} 
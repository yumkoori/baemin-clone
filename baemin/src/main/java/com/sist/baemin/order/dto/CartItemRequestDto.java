package com.sist.baemin.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartItemRequestDto {
    private Long storeId;
    private Long menuId;
    private Integer quantity;
    private List<CartOptionRequestDto> options;
} 
package com.sist.baemin.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDetailDto {
    private Long cartItemId;
    private Long menuId;
    private String menuName;
    private Integer price;
    private Integer quantity;
    private Integer totalPrice;
    
    public CartItemDetailDto() {}
    
    public CartItemDetailDto(Long cartItemId, Long menuId, String menuName, Integer price, Integer quantity) {
        this.cartItemId = cartItemId;
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity;
    }
}

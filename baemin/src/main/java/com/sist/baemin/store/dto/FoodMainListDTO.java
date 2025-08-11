package com.sist.baemin.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodMainListDTO {
    private String storeName;
    private BigDecimal rating;
    private Long reviewCount;
    private Integer minimumPrice;
    private Integer deliveryFee;
    private Long storeId;
}

package com.sist.baemin.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodListDTO {
    private String storeName;
    private BigDecimal rating;
    private Long reviewCount;
    private Integer minimumPrice;
    private Integer deliveryFee;
}

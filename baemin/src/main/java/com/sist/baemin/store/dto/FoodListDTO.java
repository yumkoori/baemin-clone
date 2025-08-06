package com.sist.baemin.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodListDTO {
    private String storeName;
    private Integer rating;
    private Long reviewCount;
    private Integer minimumPrice;
    private Integer deliveryFee;
}

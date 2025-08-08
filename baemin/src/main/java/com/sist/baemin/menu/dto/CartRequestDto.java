package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CartRequestDto {
    private Long menuId;
    private Long storeId;
    private Integer quantity;
    private List<Long> selectedOptionIds;
    private String specialRequest; // 특별 요청사항
} 
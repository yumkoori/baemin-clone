package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuResponseDto {
    // 메뉴 기본 정보
    private Long menuId;
    private String menuName;
    private String description;
    private Integer price;
    private String imageUrl;
    private Boolean isAvailable;
    
    // 가게 정보
    private Long storeId;
    private String storeName;
    
    // 카테고리 정보
    private Long categoryId;
    private String categoryName;
    
    // 추가 정보
    private Boolean isPopular;
    private Boolean isRecommended;
    private Integer reviewCount;
} 
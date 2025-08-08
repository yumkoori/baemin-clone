package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MenuDetailResponseDto {
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
    
    // 메뉴 옵션 목록
    private List<MenuOptionResponseDto> options;
    
    // HTML 템플릿에서 사용되는 추가 필드들
    private Boolean isPopular;
    private Boolean isRecommended;
    private Integer reviewCount;
} 
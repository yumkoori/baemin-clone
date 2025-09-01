package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequestDto {
    
    private String type; // "store" 또는 "menu"
    private Long targetId; // storeId 또는 menuId
}
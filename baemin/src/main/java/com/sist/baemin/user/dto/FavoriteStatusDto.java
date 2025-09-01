package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusDto {
    
    private Boolean isFavorite;
    private Long favoriteId;
}
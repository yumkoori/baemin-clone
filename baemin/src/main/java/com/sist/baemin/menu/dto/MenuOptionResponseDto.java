package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuOptionResponseDto {
    private Long optionId;
    private String optionName;
    private String description;
    private Integer additionalPrice;
    private Boolean isRequired;
    private Boolean isAvailable;
} 
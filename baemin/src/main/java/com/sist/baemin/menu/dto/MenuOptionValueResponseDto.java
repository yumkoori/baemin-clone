package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuOptionValueResponseDto {
    private Long optionValueId;
    private String optionValue;
    private Integer additionalPrice;
    private Integer displayOrder;
    private Boolean isAvailable;
}

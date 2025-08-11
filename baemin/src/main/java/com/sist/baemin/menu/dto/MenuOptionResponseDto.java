package com.sist.baemin.menu.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MenuOptionResponseDto {
    private Long optionId;
    private String optionName;
    private String description;
    private Integer additionalPrice;
    private Boolean isRequired;
    private Boolean isAvailable;
    private Boolean isMultiple;
    private List<MenuOptionValueResponseDto> optionValues;
} 
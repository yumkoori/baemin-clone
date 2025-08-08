package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressCreateDto {
    private String alias;
    private String zipCode;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;
}

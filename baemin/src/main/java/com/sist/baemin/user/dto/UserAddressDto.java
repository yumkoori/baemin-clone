package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDto {
    private Long id;
    private String alias;
    private String zipCode;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;
}

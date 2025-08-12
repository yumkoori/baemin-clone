package com.sist.baemin.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserAddressDto {
    private Long id;
    private String alias;
    private String zipCode;
    private String roadAddress;
    private String detailAddress;
    private boolean isDefault;
}

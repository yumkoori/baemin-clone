package com.sist.baemin.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressCreateDto {
    private String alias;
    private String zipCode;
    private String roadAddress;
    private String detailAddress;
    @JsonProperty("isDefault")
    private boolean isDefault;
    private BigDecimal latitude;   // 위도
    private BigDecimal longitude;  // 경도
}

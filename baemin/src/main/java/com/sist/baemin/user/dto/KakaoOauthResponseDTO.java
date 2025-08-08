package com.sist.baemin.user.dto;

import lombok.ToString;

@ToString
public class KakaoOauthResponseDTO {
    private String code;

    private String error;

    private String error_description;

    private String state;
}

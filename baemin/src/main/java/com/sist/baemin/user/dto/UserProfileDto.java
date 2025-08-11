package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String nickname;
    private String realName;
    private String email;
    private String profileImageUrl;
    private String defaultAddress;
} 
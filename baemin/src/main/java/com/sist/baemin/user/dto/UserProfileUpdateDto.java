package com.sist.baemin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDto {
    private String nickname;
    private String realName;
    private String email;
} 
package com.sist.baemin.user.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileDto {
    private String nickname;
    private String realName;
    private String email;
    private String profileImageUrl;
    private Long couponCount;
    private Long point;
}
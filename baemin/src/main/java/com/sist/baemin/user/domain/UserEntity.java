package com.sist.baemin.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    private String email;

    private String password;

    private String nickname;

    private String role;        //나중에 enum으로 변경

    private String name;

    private String tier;        //나중에 enum으로 변경

    private String profileImage;

}

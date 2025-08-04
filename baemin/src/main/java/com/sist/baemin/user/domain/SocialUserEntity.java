package com.sist.baemin.user.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "socialUser")
public class SocialUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "socialUserId")
    private Long socialUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(name = "provider")
    private String provider;        //enum으로 변경 가능성

    @Column(name = "providerId")
    private String providerId;
}

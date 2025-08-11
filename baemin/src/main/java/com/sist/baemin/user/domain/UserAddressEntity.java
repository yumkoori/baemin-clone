package com.sist.baemin.user.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "userAddress")
public class UserAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addressId")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(name = "roadAddress")
    private String roadAddress;

    @Column(name = "detailAddress")
    private String detailAddress;

    @Column(name = "addressName")
    private String addressName;

    @Column(name = "zipCode")
    private String zipCode;

    @Column(name = "isDefault")
    private boolean isDefault;
}

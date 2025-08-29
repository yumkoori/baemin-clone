package com.sist.baemin.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "userAddress")
@Getter
@Setter
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

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
}

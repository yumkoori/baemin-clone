package com.sist.baemin.store.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store")
@Getter
@Setter
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storeId", length = 50)
    private Long storeId;

    @Column(name = "storeName", nullable = false, length = 50)
    private String storeName;

    @Column(name = "phoneNumber", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "storeAdress", nullable = false, length = 50)
    private String storeAdress;

    @Column(name = "minimumPrice", nullable = false)
    private int minimumPrice;

    @Column(name = "openAt", nullable = false)
    private LocalDateTime openAt;

    @Column(name = "closeAt", nullable = false)
    private LocalDateTime closeAt;

    @Column(name = "mainImage", nullable = false, length = 255)
    private String mainImage;

    @Column(name = "deliveryFee", nullable = false)
    private int deliveryFee;

    @Column(name = "registerAt", nullable = false)
    private LocalDateTime registerAt;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

}

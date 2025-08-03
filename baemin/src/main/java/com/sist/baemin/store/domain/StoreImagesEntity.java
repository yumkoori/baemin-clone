package com.sist.baemin.store.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "storeImages")
@Getter
@Setter
public class StoreImagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storeImageId", length = 50)
    private Long storeImageId;

    @ManyToOne
    @JoinColumn(name = "storeId", nullable = false)
    private StoreEntity store;
}

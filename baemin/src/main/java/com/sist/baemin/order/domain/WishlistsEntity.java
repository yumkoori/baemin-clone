package com.sist.baemin.order.domain;

import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
public class WishlistsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlistId")
    private Long wishlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId")
    private StoreEntity store;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt;
}

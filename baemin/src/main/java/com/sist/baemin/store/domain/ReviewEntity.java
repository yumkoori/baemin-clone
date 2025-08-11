package com.sist.baemin.store.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.user.domain.UserEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private UserEntity user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId")
    private StoreEntity store;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private OrderEntity order;
    
    @Column(name = "rating")
    private int rating; // 1~5점
    
    @Column(name = "content")
    private String content;
    
    @Column(name = "createdAt")
    private LocalDateTime createdAt;


}

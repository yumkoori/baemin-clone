package com.sist.baemin.order.domain;

import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId", nullable = false)
    private StoreEntity store;
    
    @Column(name = "totalPrice")
    private BigDecimal totalPrice;
    
    @Column(name = "orderStatus")
    private String orderStatus;
    
    @Column(name = "deliveryAddress")
    private String deliveryAddress;
    
    @Column(name = "requirements")
    private String requirements;
    
    @Column(name = "paymentMethod")
    private String paymentMethod;
    
    @Column(name = "orderCreatedAt")
    private LocalDateTime orderCreatedAt;
    

}

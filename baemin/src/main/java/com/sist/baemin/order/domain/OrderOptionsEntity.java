package com.sist.baemin.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orderOptions")
@Getter
@Setter
@NoArgsConstructor
public class OrderOptionsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderOptionId")
    private Long orderOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderItemId", nullable = false)
    private OrderItemsEntity orderItem;
}

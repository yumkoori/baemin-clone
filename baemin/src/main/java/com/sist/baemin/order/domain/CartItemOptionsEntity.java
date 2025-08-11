package com.sist.baemin.order.domain;

import com.sist.baemin.menu.domain.MenuOptionEntity;
import com.sist.baemin.menu.domain.MenuOptionValueEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cartItemOptions")
@Getter
@Setter
@NoArgsConstructor
public class CartItemOptionsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartItemOptionId")
    private Long cartItemOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartItemId", nullable = false)
    private CartItemEntity cartItem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuOptionId", nullable = false)
    private MenuOptionEntity menuOption;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuOptionValueId", nullable = false)
    private MenuOptionValueEntity menuOptionValue;
}
package com.sist.baemin.menu.domain;

import com.sist.baemin.order.domain.OrderOptionsEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menuOption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOptionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuOptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuId")
    private MenuEntity menu;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderOptionId")
    private OrderOptionsEntity orderOption;
    
    @Column(name = "optionName")
    private String optionName;
    
    @Column(name = "isRequired")
    private Boolean isRequired;
    
    @Column(name = "isMultiple")
    private Boolean isMultiple;
 
}

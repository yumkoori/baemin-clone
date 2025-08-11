package com.sist.baemin.menu.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menuOptionValue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOptionValueEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menuOptionValueId")
    private Long menuOptionValueId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuOptionId", nullable = false)
    private MenuOptionEntity menuOption;
    
    @Column(name = "optionValue", nullable = false)
    private String optionValue;
    
    @Column(name = "additionalPrice")
    private Integer additionalPrice;
    
    @Column(name = "displayOrder")
    private Integer displayOrder;
    
    @Column(name = "isAvailable")
    private Boolean isAvailable;
}
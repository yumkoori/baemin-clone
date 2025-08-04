package com.sist.baemin.menu.domain;

import java.util.ArrayList;
import java.util.List;

import com.sist.baemin.store.domain.StoreEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "메뉴")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;
    
    @ManyToOne
    @JoinColumn(name = "storeId")
    private StoreEntity store;
    
    @Column(name = "menuName", nullable = false)
    private String menuName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "price")
    private int price;
    
    @Column(name = "imageUrl")
    private String imageUrl;
    
    @Column(name = "isAvailable")
    private Boolean isAvailable;

}

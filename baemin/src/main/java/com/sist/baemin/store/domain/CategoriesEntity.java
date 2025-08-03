package com.sist.baemin.store.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class CategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoriesId", length = 50)
    private Long categoriesId;

    @Column(name = "categoriesName", nullable = false, length = 100)
    private String categoriesName;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreCategoriesEntity> storeCategories = new ArrayList<>();
}


package com.sist.baemin.store.repository;

import com.sist.baemin.store.domain.CategoriesEntity;
import com.sist.baemin.store.dto.FoodListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FilterFoodListRepoitory extends JpaRepository<CategoriesEntity,Long>{

    @Query(value = """
        SELECT 
          s.store_name                               AS storeName,
          AVG(r.rating)                              AS rating,
          COUNT(r.content)                           AS reviewCount,
          s.minimum_price                            AS minimumPrice,
          s.delivery_fee                             AS deliveryFee
        FROM categories c
        JOIN store_categories sc ON c.categories_id = sc.categories_id
        JOIN store s             ON s.store_id          = sc.store_id
        LEFT JOIN review r        ON s.store_id          = r.store_id
        WHERE c.categories_id = :category
        GROUP BY 
          s.store_name,
          s.minimum_price,
          s.delivery_fee
        ORDER BY 
          rating DESC
        """,
            nativeQuery = true
    )
    public List<FoodListDTO> filterRatingFoodList(int category);


    @Query(value = """
        SELECT 
          s.store_name                               AS storeName,
          AVG(r.rating)                              AS rating,
          COUNT(r.content)                           AS reviewCount,
          s.minimum_price                            AS minimumPrice,
          s.delivery_fee                             AS deliveryFee
        FROM categories c
        JOIN store_categories sc ON c.categories_id = sc.categories_id
        JOIN store s             ON s.store_id          = sc.store_id
        LEFT JOIN review r        ON s.store_id          = r.store_id
        WHERE c.categories_id = :category
        GROUP BY 
          s.store_name,
          s.minimum_price,
          s.delivery_fee
        ORDER BY 
          delivery_fee ASC
        """,
            nativeQuery = true
    )
    public List<FoodListDTO> filterDeliveryFeeFoodList(int category);


    @Query(value = """
        SELECT 
          s.store_name                               AS storeName,
          AVG(r.rating)                              AS rating,
          COUNT(r.content)                           AS reviewCount,
          s.minimum_price                            AS minimumPrice,
          s.delivery_fee                             AS deliveryFee
        FROM categories c
        JOIN store_categories sc ON c.categories_id = sc.categories_id
        JOIN store s             ON s.store_id          = sc.store_id
        LEFT JOIN review r        ON s.store_id          = r.store_id
        WHERE c.categories_id = :category
        GROUP BY 
          s.store_name,
          s.minimum_price,
          s.delivery_fee
        ORDER BY 
          minimum_price ASC
        """,
            nativeQuery = true
    )
    public List<FoodListDTO> filterminimumPriceFoodList(int category);
}

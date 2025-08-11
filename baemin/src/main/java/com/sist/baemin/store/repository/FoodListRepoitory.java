package com.sist.baemin.store.repository;

import com.sist.baemin.store.domain.CategoriesEntity;
import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.dto.FoodMainListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodListRepoitory extends JpaRepository<CategoriesEntity, Long> {

    @Query(value = """
SELECT
    s.store_name                            AS storeName,
    CAST(AVG(r.rating) AS DECIMAL(3,2))     AS rating,
    CAST(COUNT(r.content) AS SIGNED)        AS reviewCount,
    s.minimum_price                         AS minimumPrice,
    s.delivery_fee                          AS deliveryFee,
    s.store_id                              AS storeId
FROM categories c
JOIN store_categories sc ON c.categories_id = sc.categories_id
JOIN store s             ON s.store_id = sc.store_id
JOIN review r            ON s.store_id = r.store_id
WHERE c.categories_id = :category
GROUP BY
    s.store_name,
    s.minimum_price,
    s.delivery_fee
        """, nativeQuery = true)
    List<FoodMainListDTO> getBasicFoodList(@Param("category") int category);
}

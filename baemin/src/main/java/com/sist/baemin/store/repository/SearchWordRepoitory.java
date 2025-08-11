package com.sist.baemin.store.repository;

import com.sist.baemin.store.domain.CategoriesEntity;
import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.dto.FoodMainListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchWordRepoitory extends JpaRepository<CategoriesEntity,Long> {

    @Query(value = """
        SELECT 
          s.store_name                               AS storeName,
          AVG(r.rating)                              AS rating,
          COUNT(r.content)                           AS reviewCount,
          s.minimum_price                            AS minimumPrice,
          s.delivery_fee                             AS deliveryFee,
          s.store_id                                 AS storeId
        FROM categories c
        JOIN store_categories sc ON c.categories_id = sc.categories_id
        JOIN store s             ON s.store_id          = sc.store_id
        LEFT JOIN review r        ON s.store_id          = r.store_id
        WHERE s.store_name = :keyword
        GROUP BY 
          s.store_name,
          s.minimum_price,
          s.delivery_fee
        """,
            nativeQuery = true
    )
    public List<FoodMainListDTO> searchKeyWord(String keyword);
}

package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    
    @Query("SELECT c FROM CartEntity c WHERE c.user.userId = :userId")
    Optional<CartEntity> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CartEntity c WHERE c.user.userId = :userId AND c.store.storeId = :storeId")
    Optional<CartEntity> findByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);
    
    @Query("SELECT c FROM CartEntity c WHERE c.cartId = :cartId")
    Optional<CartEntity> findByCartId(@Param("cartId") Long cartId);
}
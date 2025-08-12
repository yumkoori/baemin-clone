package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.cart.cartId = :cartId")
    List<CartItemEntity> findByCartId(@Param("cartId") Long cartId);
    
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.cart.cartId = :cartId AND ci.menu.menuId = :menuId")
    List<CartItemEntity> findByCartIdAndMenuId(@Param("cartId") Long cartId, @Param("menuId") Long menuId);
    
    void deleteByCartCartId(Long cartId);
}
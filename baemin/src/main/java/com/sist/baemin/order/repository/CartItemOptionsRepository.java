package com.sist.baemin.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sist.baemin.order.domain.CartItemOptionsEntity;

public interface CartItemOptionsRepository extends JpaRepository<CartItemOptionsEntity, Long> {

    @Query("select cio from CartItemOptionsEntity cio " +
           "join fetch cio.cartItem ci " +
           "join fetch ci.menu m " +
           "join fetch ci.cart c " +
           "join fetch c.store s " +
           "join fetch cio.menuOption mo " +
           "join fetch cio.menuOptionValue mov " +
           "where cio.cartItemOptionId = :id")
    Optional<CartItemOptionsEntity> findByIdWithAllJoins(@Param("id") Long id);
}



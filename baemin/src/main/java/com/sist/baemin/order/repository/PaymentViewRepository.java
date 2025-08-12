package com.sist.baemin.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sist.baemin.order.domain.CartItemOptionsEntity;

/**
 * 결제(주문) 상세 화면 구성을 위한 조회 전용 리포지토리
 */
public interface PaymentViewRepository extends JpaRepository<CartItemOptionsEntity, Long> {

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



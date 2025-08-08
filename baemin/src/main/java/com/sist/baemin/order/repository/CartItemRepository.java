package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    // 사용자별 모든 장바구니 아이템 조회
    List<CartItemEntity> findByCart_User(UserEntity user);
} 
package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.store.domain.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    // 사용자와 가게별 장바구니 조회
    Optional<CartEntity> findByUserAndStore(UserEntity user, StoreEntity store);
} 
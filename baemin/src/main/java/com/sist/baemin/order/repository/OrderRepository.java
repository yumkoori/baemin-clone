package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    // 사용자별 주문 목록 조회
    List<OrderEntity> findByUser_UserIdOrderByOrderCreatedAtDesc(Long userId);
    
    // 가게별 주문 목록 조회
    List<OrderEntity> findByStore_StoreIdOrderByOrderCreatedAtDesc(Long storeId);
    
    // 주문 상태별 조회
    List<OrderEntity> findByOrderStatusOrderByOrderCreatedAtDesc(String orderStatus);
    
    // 보류 상태이면서 생성 시간이 특정 시간보다 이전인 주문 조회 (30분 경과)
    List<OrderEntity> findByOrderStatusAndOrderCreatedAtBefore(String orderStatus, LocalDateTime dateTime);
}



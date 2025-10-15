package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.OrderItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItemsEntity, Long> {
    
    // 주문별 주문 아이템 조회
    List<OrderItemsEntity> findByOrder_OrderId(Long orderId);
}



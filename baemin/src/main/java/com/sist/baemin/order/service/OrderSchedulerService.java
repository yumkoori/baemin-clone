package com.sist.baemin.order.service;

import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSchedulerService {

    private final OrderRepository orderRepository;
    
    /**
     * 보류 상태의 주문을 30분마다 체크하여 30분이 지난 주문을 자동 삭제합니다.
     * 매 10분마다 실행되며, 생성된 지 30분이 지난 보류 상태의 주문을 찾아서 삭제합니다.
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행 (600,000ms = 10분)
    @Transactional
    public void deleteExpiredPendingOrders() {
        log.info("=== 보류 상태 주문 삭제 스케줄러 시작 ===");
        
        try {
            // 현재 시간 기준 30분 전
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
            
            // PENDING_PAYMENT 상태이면서 30분이 지난 주문 조회
            List<OrderEntity> expiredOrders = orderRepository
                    .findByOrderStatusAndOrderCreatedAtBefore("PENDING_PAYMENT", thirtyMinutesAgo);
            
            if (expiredOrders.isEmpty()) {
                log.info("삭제할 보류 상태 주문이 없습니다.");
                return;
            }
            
            log.info("삭제 대상 주문 수: {}", expiredOrders.size());
            
            // 주문 삭제
            for (OrderEntity order : expiredOrders) {
                log.info("주문 삭제 - orderId: {}, 생성시간: {}, 상태: {}", 
                        order.getOrderId(), 
                        order.getOrderCreatedAt(), 
                        order.getOrderStatus());
                orderRepository.delete(order);
            }
            
            log.info("총 {}건의 보류 상태 주문이 삭제되었습니다.", expiredOrders.size());
            
        } catch (Exception e) {
            log.error("보류 상태 주문 삭제 중 오류 발생", e);
        }
        
        log.info("=== 보류 상태 주문 삭제 스케줄러 종료 ===");
    }
    
    /**
     * 테스트용 메서드 - 즉시 실행하여 만료된 주문을 삭제합니다.
     */
    public int deleteExpiredPendingOrdersNow() {
        log.info("수동으로 보류 상태 주문 삭제 실행");
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<OrderEntity> expiredOrders = orderRepository
                .findByOrderStatusAndOrderCreatedAtBefore("PENDING_PAYMENT", thirtyMinutesAgo);
        
        orderRepository.deleteAll(expiredOrders);
        log.info("{}건의 보류 상태 주문이 삭제되었습니다.", expiredOrders.size());
        
        return expiredOrders.size();
    }
}

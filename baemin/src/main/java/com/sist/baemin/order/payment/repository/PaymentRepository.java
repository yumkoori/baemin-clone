package com.sist.baemin.order.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sist.baemin.order.payment.domain.PaymentEntity;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
	
	// 포트원 결제 ID로 조회
	Optional<PaymentEntity> findByPortonePaymentId(String portonePaymentId);
	
	// 주문 ID로 조회
	Optional<PaymentEntity> findByOrder_OrderId(Long orderId);
}



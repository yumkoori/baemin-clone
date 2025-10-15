package com.sist.baemin.order.payment.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.sist.baemin.order.domain.OrderEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "payment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class PaymentEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;
	
	@OneToOne
	@JoinColumn(name = "orderId")
	private OrderEntity order;

	// 포트원 결제 고유 ID
	@Column(unique = true)
	private String portonePaymentId;
	
	// 포트원 거래 ID
	private String transactionId;

	// 결제 상태 (PENDING, PAID, FAILED, CANCELLED)
    private String paymentStatus;

	// 결제 금액
    private Long paymentPrice;

	// 사전 검증 금액 (결제 요청 전 서버에서 계산한 금액)
	private Long expectedAmount;
	
	// 결제 수단
	private String paymentMethod;
	
	// 주문명
	private String orderName;
	
	// 구매자 정보
	private String buyerName;
	private String buyerEmail;
	private String buyerTel;
	
	// 주문 생성을 위한 정보
	private Long userId;
	private Long storeId;
	private Long cartId;

	// 결제 시작 시간
	private LocalDateTime createdAt;
	
	// 결제 완료/실패 시간
    private LocalDateTime paymentAt;
    
    // 최종 검증 완료 여부
    private Boolean verified;
    
    // 실패 사유
    private String failReason;
	
}

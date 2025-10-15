package com.sist.baemin.order.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 사전 검증 데이터 저장 요청 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareRequest {
	
	private String portonePaymentId; // 포트원에서 생성한 결제 ID
	private Long totalAmount; // 총 결제 금액
	private String paymentMethod; // 결제 수단
	private String orderName; // 주문명
	private String buyerName; // 구매자명
	private String buyerEmail; // 구매자 이메일
	private String buyerTel; // 구매자 전화번호
}



package com.sist.baemin.order.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 사전 검증 데이터 저장 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPrepareResponse {
	
	private boolean success;
	private String message;
	private String portonePaymentId;
	private Long expectedAmount;
}



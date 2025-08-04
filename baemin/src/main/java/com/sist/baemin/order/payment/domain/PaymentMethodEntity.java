package com.sist.baemin.order.payment.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class PaymentMethodEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentMethodId;
	
	private String methodName;
	
}


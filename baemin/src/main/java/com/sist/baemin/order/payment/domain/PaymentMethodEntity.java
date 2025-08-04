package com.sist.baemin.order.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "paymentMethod")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class PaymentMethodEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentMethodId;
	
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "paymentId")
	private PaymentEntity payment;
	private String methodName;
	
}


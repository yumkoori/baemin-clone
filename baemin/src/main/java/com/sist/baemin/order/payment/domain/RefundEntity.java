package com.sist.baemin.order.payment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class RefundEntity{	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long refundId;
	
	@OneToOne
	@JoinColumn(name = "paymentId")
	private PaymentEntity payment;
	
	private LocalDateTime refundAt;
}


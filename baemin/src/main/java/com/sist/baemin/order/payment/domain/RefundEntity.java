package com.sist.baemin.order.payment.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "refund")
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


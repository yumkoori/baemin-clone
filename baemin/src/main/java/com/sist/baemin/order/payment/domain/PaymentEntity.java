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

    private String paymentStatus;

    private Long paymentPrice;

    private LocalDateTime paymentAt;
	
}

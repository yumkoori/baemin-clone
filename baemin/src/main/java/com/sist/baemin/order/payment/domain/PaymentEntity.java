package com.sist.baemin.order.payment.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.sist.baemin.order.domain.OrderEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
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
	
	@ToString.Exclude
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="paymentMethodId")
	private List<PaymentMethodEntity> paymentMethod;
	
	private String paymentStatus;
	
	private Long paymentPrice;
	
	private LocalDateTime paymentAt;
	
}

package com.sist.baemin.user.domain;

import java.time.LocalDateTime;

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
public class CouponEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long couponId;
	
	private String couponName;
	private String couponCode;
	private String description;
	private int discountAmount;
	private int minOrderAmount;
	private LocalDateTime expiryDate;
	
	
}

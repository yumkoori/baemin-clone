package com.sist.baemin.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
public class UserCouponEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userCouponId;
	
	@ManyToOne
	@JoinColumn(name="userId")
	private UserEntity user;
	
	@ManyToOne
	@JoinColumn(name="couponId")
	private CouponEntity coupon;
	
	private boolean isUsed;
	private LocalDateTime usedAt;
	private LocalDateTime issuedAt;
	
	
}

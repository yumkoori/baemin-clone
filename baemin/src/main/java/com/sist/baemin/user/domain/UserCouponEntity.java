package com.sist.baemin.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@Table(name = "userCoupon")
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
    
    // 사용자 보유 쿠폰의 만료 시각 (이벤트 스케줄러 대상)
    private LocalDateTime expiresAt;
	
	
}

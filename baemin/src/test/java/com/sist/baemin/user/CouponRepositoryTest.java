package com.sist.baemin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.sist.baemin.user.domain.CouponEntity;
import com.sist.baemin.user.repository.CouponRepository;

@SpringBootTest
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("쿠폰 저장 및 조회")
    @Transactional
    @Rollback
    void saveCoupon() {
        CouponEntity coupon = CouponEntity.builder()
                .couponName("첫주문 2천원 할인")
                .couponCode("WELCOME2000")
                .description("1만원 이상 주문 시 2,000원 할인")
                .discountAmount(2000)
                .minOrderAmount(10000)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();

        CouponEntity saved = couponRepository.save(coupon);

        assertThat(saved.getCouponId()).isNotNull();
        assertThat(saved.getCouponName()).isEqualTo("첫주문 2천원 할인");
    }
}



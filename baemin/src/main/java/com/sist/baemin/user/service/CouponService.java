package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.CouponEntity;
import com.sist.baemin.user.domain.UserCouponEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.CouponRepository;
import com.sist.baemin.user.repository.UserCouponRepository;
import com.sist.baemin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    
    // 고정 쿠폰 ID (5천원 할인 이벤트용)
    private static final Long EVENT_COUPON_ID = 1L;
    
    /**
     * 사용자에게 쿠폰 발급 (고정 쿠폰 ID 사용)
     */
    @Transactional
    public UserCouponEntity issueCouponToUser(Long userId) {
        // 중복 발급 확인
        List<UserCouponEntity> userCoupons = userCouponRepository.findAll();
        boolean alreadyIssued = userCoupons.stream()
                .anyMatch(uc -> uc.getUser().getUserId().equals(userId) 
                        && uc.getCoupon().getCouponId().equals(EVENT_COUPON_ID)
                        && !uc.isUsed());
        
        if (alreadyIssued) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        CouponEntity coupon = couponRepository.findById(EVENT_COUPON_ID)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        
        // 만료일 설정 (발급일로부터 30일)
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);
        
        // 사용자 쿠폰 생성
        UserCouponEntity userCoupon = UserCouponEntity.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiryDate)
                .build();
        
        return userCouponRepository.save(userCoupon);
    }
    
    /**
     * 사용자의 사용 가능한 쿠폰 목록 조회
     */
    public List<UserCouponEntity> getAvailableUserCoupons(Long userId) {
        List<UserCouponEntity> allUserCoupons = userCouponRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        return allUserCoupons.stream()
                .filter(uc -> uc.getUser().getUserId().equals(userId))
                .filter(uc -> !uc.isUsed())
                .filter(uc -> uc.getExpiresAt().isAfter(now))
                .collect(Collectors.toList());
    }
    
    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public void useCoupon(Long userCouponId) {
        UserCouponEntity userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        
        if (userCoupon.isUsed()) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        
        if (userCoupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        
        // 쿠폰 사용 처리는 UserCouponEntity가 불변이므로 새로운 엔티티로 저장해야 함
        // 하지만 JPA에서는 수정 메서드가 필요함
        // 임시로 삭제 후 재생성하거나, 엔티티에 setter 추가 필요
    }
}


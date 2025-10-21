package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.CouponEntity;
import com.sist.baemin.user.domain.UserCouponEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.dto.CouponCacheDto;
import com.sist.baemin.user.repository.CouponRepository;
import com.sist.baemin.user.repository.UserCouponRepository;
import com.sist.baemin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;
    private final CouponRedisService couponRedisService; // Redis 서비스 추가
    
    // 고정 쿠폰 ID (5천원 할인 이벤트용)
    private static final Long EVENT_COUPON_ID = 1L;
    
    // 쿠폰 만료 시간 (72시간 = 3일)
    private static final long COUPON_EXPIRY_HOURS = 72;
    
    // 선착순 제한 인원
    private static final long MAX_COUPON_STOCK = 100;
    
    /**
     * 사용자에게 쿠폰 발급 (고정 쿠폰 ID 사용)
     * Redis를 활용한 중복 체크 및 통계
     */
    @Transactional
    public UserCouponEntity issueCouponToUser(Long userId) {
        log.info("쿠폰 발급 요청 - User ID: {}, Coupon ID: {}", userId, EVENT_COUPON_ID);
        
        // 1) 선착순 재고 체크 (100명 제한)
        if (!couponRedisService.checkAndDecrementStock(EVENT_COUPON_ID, MAX_COUPON_STOCK)) {
            log.warn("쿠폰 재고 소진 - Coupon ID: {}", EVENT_COUPON_ID);
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        
        // 2) Redis로 빠르게 중복 발급 확인 (72시간 기준)
        if (couponRedisService.isAlreadyIssued(userId, EVENT_COUPON_ID)) {
            log.warn("중복 발급 감지 (Redis) - User ID: {}", userId);
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }
        
        // 3) DB에서도 한 번 더 확인 (이중 체크)
        List<UserCouponEntity> userCoupons = userCouponRepository.findAll();
        boolean alreadyIssued = userCoupons.stream()
                .anyMatch(uc -> uc.getUser().getUserId().equals(userId) 
                        && uc.getCoupon().getCouponId().equals(EVENT_COUPON_ID)
                        && !uc.isUsed());
        
        if (alreadyIssued) {
            log.warn("중복 발급 감지 (DB) - User ID: {}", userId);
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        CouponEntity coupon = couponRepository.findById(EVENT_COUPON_ID)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        
        // 만료일 설정 (발급일로부터 72시간 = 3일)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(COUPON_EXPIRY_HOURS);
        
        // 사용자 쿠폰 생성
        UserCouponEntity userCoupon = UserCouponEntity.builder()
                .user(user)
                .coupon(coupon)
                .isUsed(false)
                .issuedAt(LocalDateTime.now())
                .expiresAt(expiryDate)
                .build();
        
        UserCouponEntity savedCoupon = userCouponRepository.save(userCoupon);
        
        // 4) Redis에 발급 기록 저장 (72시간 TTL)
        couponRedisService.recordCouponIssue(userId, EVENT_COUPON_ID);
        
        // 5) 발급 통계 증가
        Long totalIssued = couponRedisService.incrementIssueCount(EVENT_COUPON_ID);
        Long currentStock = couponRedisService.getCurrentStock(EVENT_COUPON_ID);
        log.info("✅ 쿠폰 발급 완료 - User: {}, Coupon: {}, 총 발급: {}건, 남은 재고: {}/{}", 
                userId, EVENT_COUPON_ID, totalIssued, MAX_COUPON_STOCK - currentStock, MAX_COUPON_STOCK);
        
        return savedCoupon;
    }
    
    /**
     * 쿠폰 정보 조회 (Redis 캐싱 활용)
     */
    public CouponCacheDto getCouponInfo(Long couponId) {
        return couponRedisService.getCouponInfoWithCache(couponId);
    }
    
    /**
     * 현재 발급된 쿠폰 수량 조회
     */
    public long getCurrentCouponStock() {
        return couponRedisService.getCurrentStock(EVENT_COUPON_ID);
    }
    
    /**
     * 최대 쿠폰 수량 조회
     */
    public long getMaxCouponStock() {
        return MAX_COUPON_STOCK;
    }
    
    /**
     * 쿠폰 재고 초기화 (관리자용)
     */
    @Transactional
    public void resetCouponStock() {
        couponRedisService.resetCouponStock(EVENT_COUPON_ID);
        log.info("🔄 쿠폰 재고 초기화 완료 - Coupon ID: {}", EVENT_COUPON_ID);
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


package com.sist.baemin.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sist.baemin.common.redis.RedisService;
import com.sist.baemin.user.domain.CouponEntity;
import com.sist.baemin.user.dto.CouponCacheDto;
import com.sist.baemin.user.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 쿠폰 관련 Redis 작업을 처리하는 서비스
 * - 쿠폰 정보 JSON 캐싱
 * - 중복 발급 체크 (72시간 TTL)
 * - 선착순 쿠폰 재고 관리 (INCR)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponRedisService {

    private final RedisService redisService;
    private final CouponRepository couponRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // LocalDateTime 직렬화 지원

    // Redis Key 상수
    private static final String COUPON_INFO_KEY = "coupon:info:%d"; // 쿠폰 정보 캐시
    private static final String COUPON_ISSUE_KEY = "coupon:issue:user:%d:coupon:%d"; // 발급 체크
    private static final String COUPON_STOCK_KEY = "coupon:stock:%d"; // 선착순 재고
    private static final String COUPON_ISSUE_COUNT_KEY = "coupon:issue:count:%d"; // 발급 통계

    private static final long COUPON_CACHE_TTL = 24; // 쿠폰 정보 캐시 24시간
    private static final long ISSUE_CHECK_TTL = 72; // 중복 발급 체크 72시간

    /**
     * 1. 쿠폰 정보를 JSON으로 Redis에 캐싱
     * MariaDB 조회 후 Redis에 JSON 형태로 저장
     * 
     * @param couponId 쿠폰 ID
     * @return 쿠폰 정보 DTO
     */
    public CouponCacheDto getCouponInfoWithCache(Long couponId) {
        String key = String.format(COUPON_INFO_KEY, couponId);
        
        // 1) Redis에서 먼저 조회
        String cachedJson = redisService.getValue(key);
        
        if (cachedJson != null) {
            log.info("✅ Redis Cache Hit - Coupon ID: {}", couponId);
            try {
                return objectMapper.readValue(cachedJson, CouponCacheDto.class);
            } catch (JsonProcessingException e) {
                log.error("JSON 역직렬화 실패: {}", e.getMessage());
                // 캐시 삭제
                redisService.delete(key);
            }
        }
        
        // 2) Cache Miss → DB에서 조회
        log.info("❌ Redis Cache Miss - Coupon ID: {} (DB 조회)", couponId);
        CouponEntity coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        
        CouponCacheDto dto = CouponCacheDto.fromEntity(coupon);
        
        // 3) Redis에 JSON으로 캐싱 (24시간 TTL)
        try {
            String jsonValue = objectMapper.writeValueAsString(dto);
            redisService.setValue(key, jsonValue, COUPON_CACHE_TTL, TimeUnit.HOURS);
            log.info("💾 Redis에 쿠폰 정보 캐싱 완료 - Coupon ID: {}, JSON: {}", couponId, jsonValue);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
        }
        
        return dto;
    }

    /**
     * 쿠폰 캐시 삭제 (쿠폰 정보 수정 시 호출)
     */
    public void evictCouponCache(Long couponId) {
        String key = String.format(COUPON_INFO_KEY, couponId);
        redisService.delete(key);
        log.info("🗑️ 쿠폰 캐시 삭제 - Coupon ID: {}", couponId);
    }

    /**
     * 2. 중복 발급 체크 (Redis 활용, 72시간 TTL)
     * DB 조회 대신 Redis로 빠르게 체크
     * 
     * @param userId 사용자 ID
     * @param couponId 쿠폰 ID
     * @return 이미 발급 여부
     */
    public boolean isAlreadyIssued(Long userId, Long couponId) {
        String key = String.format(COUPON_ISSUE_KEY, userId, couponId);
        Boolean exists = redisService.hasKey(key);
        
        log.info("쿠폰 중복 발급 체크 - User: {}, Coupon: {}, Already Issued: {}", 
                userId, couponId, exists);
        
        return exists != null && exists;
    }

    /**
     * 쿠폰 발급 기록 (Redis에 저장, 72시간 TTL)
     * 
     * @param userId 사용자 ID
     * @param couponId 쿠폰 ID
     */
    public void recordCouponIssue(Long userId, Long couponId) {
        String key = String.format(COUPON_ISSUE_KEY, userId, couponId);
        redisService.setValueWithTTL(key, "issued"); // 72시간 TTL 자동 설정
        
        log.info("✅ 쿠폰 발급 기록 저장 (Redis) - User: {}, Coupon: {}, TTL: 72h", userId, couponId);
    }

    /**
     * 3. 선착순 쿠폰 재고 관리 (INCR 활용)
     * 선착순 N명에게만 발급되는 쿠폰의 재고 관리
     * 
     * @param couponId 쿠폰 ID
     * @param maxStock 최대 재고
     * @return 발급 가능 여부
     */
    public boolean checkAndDecrementStock(Long couponId, long maxStock) {
        String key = String.format(COUPON_STOCK_KEY, couponId);
        
        // INCR로 현재 발급 수량 증가
        Long currentCount = redisService.incrementWithTTL(key);
        
        if (currentCount > maxStock) {
            log.warn("⚠️ 쿠폰 재고 소진 - Coupon: {}, Current: {}, Max: {}", 
                    couponId, currentCount, maxStock);
            return false;
        }
        
        log.info("✅ 쿠폰 재고 차감 성공 - Coupon: {}, Remaining: {}/{}", 
                couponId, currentCount, maxStock);
        return true;
    }

    /**
     * 선착순 쿠폰 재고 초기화
     */
    public void resetCouponStock(Long couponId) {
        String key = String.format(COUPON_STOCK_KEY, couponId);
        redisService.delete(key);
        log.info("🔄 쿠폰 재고 초기화 - Coupon: {}", couponId);
    }

    /**
     * 현재 재고 확인
     */
    public Long getCurrentStock(Long couponId) {
        String key = String.format(COUPON_STOCK_KEY, couponId);
        String value = redisService.getValue(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 4. 실시간 발급 통계 (INCR 활용, 72시간 TTL)
     * 쿠폰별 발급 횟수 카운팅
     * 
     * @param couponId 쿠폰 ID
     * @return 현재 발급 횟수
     */
    public Long incrementIssueCount(Long couponId) {
        String key = String.format(COUPON_ISSUE_COUNT_KEY, couponId);
        Long count = redisService.incrementWithTTL(key);
        
        log.info("📊 쿠폰 발급 통계 증가 - Coupon: {}, Total Count: {}", couponId, count);
        return count;
    }

    /**
     * 발급 통계 조회
     */
    public Long getIssueCount(Long couponId) {
        String key = String.format(COUPON_ISSUE_COUNT_KEY, couponId);
        String value = redisService.getValue(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 발급 통계 초기화
     */
    public void resetIssueCount(Long couponId) {
        String key = String.format(COUPON_ISSUE_COUNT_KEY, couponId);
        redisService.delete(key);
        log.info("📊 발급 통계 초기화 - Coupon: {}", couponId);
    }
}


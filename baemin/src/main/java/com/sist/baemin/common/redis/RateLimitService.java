package com.sist.baemin.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis INCR과 TTL을 활용한 Rate Limiting 서비스
 * API 요청 제한, 로그인 시도 제한 등에 활용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisService redisService;

    /**
     * API 요청 제한 체크 (72시간 기준)
     * 예시: 특정 사용자가 72시간 동안 특정 API를 최대 100번까지만 호출 가능
     * 
     * @param userId 사용자 ID
     * @param apiPath API 경로
     * @param maxRequests 최대 요청 횟수
     * @return 요청 가능 여부
     */
    public boolean checkRateLimit(String userId, String apiPath, long maxRequests) {
        String key = String.format("ratelimit:user:%s:api:%s", userId, apiPath);
        
        // 현재 요청 횟수 증가 (72시간 TTL 자동 설정)
        Long currentCount = redisService.incrementWithTTL(key);
        
        if (currentCount > maxRequests) {
            log.warn("Rate limit exceeded - User: {}, API: {}, Count: {}/{}", 
                    userId, apiPath, currentCount, maxRequests);
            return false;
        }
        
        log.info("Rate limit check passed - User: {}, API: {}, Count: {}/{}", 
                userId, apiPath, currentCount, maxRequests);
        return true;
    }

    /**
     * 로그인 시도 횟수 체크 (72시간 기준)
     * 예시: 72시간 동안 5번 이상 실패하면 로그인 차단
     * 
     * @param username 사용자명
     * @param maxAttempts 최대 시도 횟수
     * @return 로그인 시도 가능 여부
     */
    public boolean checkLoginAttempts(String username, long maxAttempts) {
        String key = String.format("login:attempts:%s", username);
        
        // 현재 시도 횟수 조회
        String value = redisService.getValue(key);
        long currentAttempts = value != null ? Long.parseLong(value) : 0;
        
        if (currentAttempts >= maxAttempts) {
            Long remainingTTL = redisService.getExpire(key, TimeUnit.HOURS);
            log.warn("Login attempts exceeded - Username: {}, Attempts: {}/{}, Remaining TTL: {}h", 
                    username, currentAttempts, maxAttempts, remainingTTL);
            return false;
        }
        
        return true;
    }

    /**
     * 로그인 실패 기록 (72시간 TTL)
     * 
     * @param username 사용자명
     * @return 현재 실패 횟수
     */
    public Long recordLoginFailure(String username) {
        String key = String.format("login:attempts:%s", username);
        Long attempts = redisService.incrementWithTTL(key);
        
        log.info("Login failure recorded - Username: {}, Attempts: {}", username, attempts);
        return attempts;
    }

    /**
     * 로그인 성공 시 시도 횟수 초기화
     * 
     * @param username 사용자명
     */
    public void clearLoginAttempts(String username) {
        String key = String.format("login:attempts:%s", username);
        redisService.delete(key);
        
        log.info("Login attempts cleared - Username: {}", username);
    }

    /**
     * 주문 요청 제한 체크 (72시간 기준)
     * 예시: 비정상적인 대량 주문 방지 - 72시간 동안 최대 50개 주문
     * 
     * @param userId 사용자 ID
     * @param maxOrders 최대 주문 수
     * @return 주문 가능 여부
     */
    public boolean checkOrderLimit(String userId, long maxOrders) {
        String key = String.format("order:limit:user:%s", userId);
        
        Long currentCount = redisService.incrementWithTTL(key);
        
        if (currentCount > maxOrders) {
            Long remainingTTL = redisService.getExpire(key, TimeUnit.HOURS);
            log.warn("Order limit exceeded - User: {}, Count: {}/{}, Remaining TTL: {}h", 
                    userId, currentCount, maxOrders, remainingTTL);
            return false;
        }
        
        log.info("Order limit check passed - User: {}, Count: {}/{}", 
                userId, currentCount, maxOrders);
        return true;
    }

    /**
     * 쿠폰 다운로드 횟수 제한 (72시간 기준)
     * 예시: 이벤트 쿠폰을 72시간 동안 사용자당 1번만 다운로드 가능
     * 
     * @param userId 사용자 ID
     * @param couponId 쿠폰 ID
     * @param maxDownloads 최대 다운로드 횟수
     * @return 다운로드 가능 여부
     */
    public boolean checkCouponDownloadLimit(String userId, String couponId, long maxDownloads) {
        String key = String.format("coupon:download:user:%s:coupon:%s", userId, couponId);
        
        // 키가 존재하면 이미 다운로드한 것
        if (redisService.hasKey(key)) {
            String value = redisService.getValue(key);
            long count = Long.parseLong(value);
            
            if (count >= maxDownloads) {
                log.warn("Coupon download limit exceeded - User: {}, Coupon: {}, Count: {}", 
                        userId, couponId, count);
                return false;
            }
        }
        
        // 다운로드 기록 (72시간 TTL)
        redisService.incrementWithTTL(key);
        log.info("Coupon download recorded - User: {}, Coupon: {}", userId, couponId);
        return true;
    }

    /**
     * 리뷰 작성 제한 (72시간 기준)
     * 예시: 동일 가게에 72시간 동안 3번까지만 리뷰 작성 가능 (어뷰징 방지)
     * 
     * @param userId 사용자 ID
     * @param storeId 가게 ID
     * @param maxReviews 최대 리뷰 수
     * @return 리뷰 작성 가능 여부
     */
    public boolean checkReviewLimit(String userId, String storeId, long maxReviews) {
        String key = String.format("review:limit:user:%s:store:%s", userId, storeId);
        
        Long currentCount = redisService.incrementWithTTL(key);
        
        if (currentCount > maxReviews) {
            Long remainingTTL = redisService.getExpire(key, TimeUnit.HOURS);
            log.warn("Review limit exceeded - User: {}, Store: {}, Count: {}/{}, Remaining TTL: {}h", 
                    userId, storeId, currentCount, maxReviews, remainingTTL);
            return false;
        }
        
        log.info("Review limit check passed - User: {}, Store: {}, Count: {}/{}", 
                userId, storeId, currentCount, maxReviews);
        return true;
    }

    /**
     * 현재 카운트 조회
     * 
     * @param key Redis 키
     * @return 현재 카운트 값
     */
    public Long getCurrentCount(String key) {
        String value = redisService.getValue(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 남은 TTL 조회 (시간 단위)
     * 
     * @param key Redis 키
     * @return 남은 시간 (시간)
     */
    public Long getRemainingTTL(String key) {
        return redisService.getExpire(key, TimeUnit.HOURS);
    }
}


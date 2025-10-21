package com.sist.baemin.user.controller;

import com.sist.baemin.user.dto.CouponCacheDto;
import com.sist.baemin.user.service.CouponRedisService;
import com.sist.baemin.user.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 쿠폰 Redis 기능 테스트 컨트롤러
 * JSON 캐싱, 선착순 쿠폰, 발급 통계 등 테스트
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon/redis")
@RequiredArgsConstructor
public class CouponRedisTestController {

    private final CouponService couponService;
    private final CouponRedisService couponRedisService;

    /**
     * 1. 쿠폰 정보 조회 (Redis JSON 캐싱)
     * 첫 번째 호출: DB 조회 → Redis에 JSON 저장
     * 두 번째 호출: Redis에서 JSON 조회 (빠름)
     * 
     * GET /api/coupon/redis/info/1
     */
    @GetMapping("/info/{couponId}")
    public ResponseEntity<Map<String, Object>> getCouponInfo(@PathVariable Long couponId) {
        long startTime = System.currentTimeMillis();
        
        // Redis 캐시 활용
        CouponCacheDto couponInfo = couponService.getCouponInfo(couponId);
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("coupon", couponInfo);
        response.put("response_time_ms", endTime - startTime);
        response.put("message", "쿠폰 정보 조회 (Redis 캐싱)");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 2. 쿠폰 캐시 삭제 (쿠폰 정보 수정 시 사용)
     * DELETE /api/coupon/redis/cache/1
     */
    @DeleteMapping("/cache/{couponId}")
    public ResponseEntity<Map<String, Object>> evictCache(@PathVariable Long couponId) {
        couponRedisService.evictCouponCache(couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("message", "쿠폰 캐시가 삭제되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 중복 발급 체크 테스트
     * GET /api/coupon/redis/check-issue?userId=1&couponId=1
     */
    @GetMapping("/check-issue")
    public ResponseEntity<Map<String, Object>> checkIssue(
            @RequestParam Long userId,
            @RequestParam Long couponId) {
        
        boolean alreadyIssued = couponRedisService.isAlreadyIssued(userId, couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("couponId", couponId);
        response.put("alreadyIssued", alreadyIssued);
        response.put("message", alreadyIssued ? "이미 발급받은 쿠폰입니다." : "발급 가능합니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 선착순 쿠폰 테스트 (최대 100명)
     * POST /api/coupon/redis/limited-issue/1
     */
    @PostMapping("/limited-issue/{couponId}")
    public ResponseEntity<Map<String, Object>> limitedIssueCoupon(@PathVariable Long couponId) {
        long MAX_STOCK = 100; // 선착순 100명
        
        boolean success = couponRedisService.checkAndDecrementStock(couponId, MAX_STOCK);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("success", success);
        response.put("currentStock", couponRedisService.getCurrentStock(couponId));
        response.put("maxStock", MAX_STOCK);
        
        if (success) {
            response.put("message", "쿠폰 발급 성공!");
        } else {
            response.put("message", "쿠폰이 모두 소진되었습니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 5. 현재 재고 확인
     * GET /api/coupon/redis/stock/1
     */
    @GetMapping("/stock/{couponId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable Long couponId) {
        Long currentStock = couponRedisService.getCurrentStock(couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("currentIssuedCount", currentStock);
        response.put("message", currentStock + "개 발급됨");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 6. 재고 초기화
     * DELETE /api/coupon/redis/stock/1
     */
    @DeleteMapping("/stock/{couponId}")
    public ResponseEntity<Map<String, Object>> resetStock(@PathVariable Long couponId) {
        couponRedisService.resetCouponStock(couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("message", "선착순 쿠폰 재고가 초기화되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 7. 발급 통계 조회
     * GET /api/coupon/redis/stats/1
     */
    @GetMapping("/stats/{couponId}")
    public ResponseEntity<Map<String, Object>> getIssueStats(@PathVariable Long couponId) {
        Long issueCount = couponRedisService.getIssueCount(couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("totalIssued", issueCount);
        response.put("message", "72시간 기준 발급 통계");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 8. 발급 통계 초기화
     * DELETE /api/coupon/redis/stats/1
     */
    @DeleteMapping("/stats/{couponId}")
    public ResponseEntity<Map<String, Object>> resetStats(@PathVariable Long couponId) {
        couponRedisService.resetIssueCount(couponId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("couponId", couponId);
        response.put("message", "발급 통계가 초기화되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 9. Redis 성능 비교 테스트
     * GET /api/coupon/redis/performance-test/1?iterations=10
     */
    @GetMapping("/performance-test/{couponId}")
    public ResponseEntity<Map<String, Object>> performanceTest(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "10") int iterations) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Redis 캐시 초기화
        couponRedisService.evictCouponCache(couponId);
        
        // 첫 번째 호출 (DB 조회)
        long dbStartTime = System.currentTimeMillis();
        couponService.getCouponInfo(couponId);
        long dbEndTime = System.currentTimeMillis();
        long dbTime = dbEndTime - dbStartTime;
        
        // 이후 호출들 (Redis 캐시)
        long totalRedisTime = 0;
        for (int i = 0; i < iterations; i++) {
            long redisStartTime = System.currentTimeMillis();
            couponService.getCouponInfo(couponId);
            long redisEndTime = System.currentTimeMillis();
            totalRedisTime += (redisEndTime - redisStartTime);
        }
        long avgRedisTime = totalRedisTime / iterations;
        
        response.put("db_query_time_ms", dbTime);
        response.put("redis_avg_time_ms", avgRedisTime);
        response.put("speedup", String.format("%.2fx faster", (double) dbTime / avgRedisTime));
        response.put("iterations", iterations);
        
        return ResponseEntity.ok(response);
    }
}


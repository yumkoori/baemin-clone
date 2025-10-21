package com.sist.baemin.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기능 테스트 및 예제 컨트롤러
 * INCR, TTL(72시간) 기능 사용 예제
 */
@Slf4j
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisService redisService;

    /**
     * INCR 테스트: 키의 값을 1씩 증가
     * 예시: /api/redis/incr/page:home:views
     * 
     * 사용 사례: 페이지 조회수, API 호출 횟수 카운팅 등
     */
    @PostMapping("/incr/{key}")
    public ResponseEntity<Map<String, Object>> increment(@PathVariable String key) {
        Long value = redisService.increment(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("message", "키 값이 증가되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * INCR with TTL 테스트: 값을 증가시키고 72시간 TTL 설정
     * 예시: /api/redis/incr-ttl/user:123:login:attempts
     * 
     * 사용 사례: 로그인 시도 횟수, 임시 카운터 등 (72시간 후 자동 삭제)
     */
    @PostMapping("/incr-ttl/{key}")
    public ResponseEntity<Map<String, Object>> incrementWithTTL(@PathVariable String key) {
        Long value = redisService.incrementWithTTL(key);
        Long ttl = redisService.getExpire(key, TimeUnit.HOURS);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttl_hours", ttl);
        response.put("message", "키 값이 증가되었고 72시간 TTL이 설정되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 값 저장 with TTL (72시간)
     * 예시: POST /api/redis/value-ttl
     * Body: { "key": "session:abc123", "value": "user_data" }
     */
    @PostMapping("/value-ttl")
    public ResponseEntity<Map<String, Object>> setValueWithTTL(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        
        redisService.setValueWithTTL(key, value);
        Long ttl = redisService.getExpire(key, TimeUnit.HOURS);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttl_hours", ttl);
        response.put("message", "값이 저장되었고 72시간 TTL이 설정되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 값 조회
     * 예시: /api/redis/value/session:abc123
     */
    @GetMapping("/value/{key}")
    public ResponseEntity<Map<String, Object>> getValue(@PathVariable String key) {
        String value = redisService.getValue(key);
        Long ttl = redisService.getExpire(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttl_seconds", ttl);
        response.put("exists", value != null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * TTL 조회
     * 예시: /api/redis/ttl/user:123:token
     */
    @GetMapping("/ttl/{key}")
    public ResponseEntity<Map<String, Object>> getTTL(@PathVariable String key) {
        Long ttlSeconds = redisService.getExpire(key, TimeUnit.SECONDS);
        Long ttlHours = redisService.getExpire(key, TimeUnit.HOURS);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("ttl_seconds", ttlSeconds);
        response.put("ttl_hours", ttlHours);
        
        if (ttlSeconds == -2) {
            response.put("message", "키가 존재하지 않습니다.");
        } else if (ttlSeconds == -1) {
            response.put("message", "키는 존재하지만 만료 시간이 설정되지 않았습니다.");
        } else {
            response.put("message", "남은 만료 시간입니다.");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 키 삭제
     * 예시: /api/redis/delete/temp:data
     */
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String key) {
        Boolean result = redisService.delete(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("deleted", result);
        response.put("message", result ? "키가 삭제되었습니다." : "키가 존재하지 않습니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 패턴 검색
     * 예시: /api/redis/keys?pattern=user:*
     */
    @GetMapping("/keys")
    public ResponseEntity<Map<String, Object>> getKeys(@RequestParam String pattern) {
        Set<String> keys = redisService.getKeys(pattern);
        
        Map<String, Object> response = new HashMap<>();
        response.put("pattern", pattern);
        response.put("keys", keys);
        response.put("count", keys != null ? keys.size() : 0);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Redis 연결 테스트
     * 예시: /api/redis/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        try {
            // 테스트 키로 연결 확인
            String testKey = "test:ping";
            redisService.setValue(testKey, "pong");
            String value = redisService.getValue(testKey);
            redisService.delete(testKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "connected");
            response.put("message", "Redis 연결 성공!");
            response.put("test_result", value);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Redis 연결 실패: ", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Redis 연결 실패: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}


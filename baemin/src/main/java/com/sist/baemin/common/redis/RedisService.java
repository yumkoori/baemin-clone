package com.sist.baemin.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기본 작업을 처리하는 서비스
 * INCR, TTL(72시간) 등의 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long TTL_72_HOURS = 72; // 72시간

    /**
     * INCR 명령어: 키의 값을 1 증가
     * 키가 존재하지 않으면 0에서 시작하여 1로 설정
     * 
     * @param key Redis 키
     * @return 증가된 값
     */
    public Long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        log.info("Redis INCR - Key: {}, Value: {}", key, value);
        return value;
    }

    /**
     * INCR 명령어: 키의 값을 지정된 delta만큼 증가
     * 
     * @param key Redis 키
     * @param delta 증가할 값
     * @return 증가된 값
     */
    public Long increment(String key, long delta) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        log.info("Redis INCR - Key: {}, Delta: {}, Value: {}", key, delta, value);
        return value;
    }

    /**
     * INCR 명령어와 TTL 설정 (72시간)
     * 값을 증가시키고 자동으로 72시간 후 만료되도록 설정
     * 
     * @param key Redis 키
     * @return 증가된 값
     */
    public Long incrementWithTTL(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        setExpire(key, TTL_72_HOURS, TimeUnit.HOURS);
        log.info("Redis INCR with TTL(72h) - Key: {}, Value: {}", key, value);
        return value;
    }

    /**
     * INCR 명령어와 TTL 설정 (72시간)
     * 값을 delta만큼 증가시키고 자동으로 72시간 후 만료되도록 설정
     * 
     * @param key Redis 키
     * @param delta 증가할 값
     * @return 증가된 값
     */
    public Long incrementWithTTL(String key, long delta) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        setExpire(key, TTL_72_HOURS, TimeUnit.HOURS);
        log.info("Redis INCR with TTL(72h) - Key: {}, Delta: {}, Value: {}", key, delta, value);
        return value;
    }

    /**
     * 키에 값 설정
     * 
     * @param key Redis 키
     * @param value 저장할 값
     */
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("Redis SET - Key: {}, Value: {}", key, value);
    }

    /**
     * 키에 값 설정 (TTL 72시간)
     * 
     * @param key Redis 키
     * @param value 저장할 값
     */
    public void setValueWithTTL(String key, String value) {
        redisTemplate.opsForValue().set(key, value, Duration.ofHours(TTL_72_HOURS));
        log.info("Redis SET with TTL(72h) - Key: {}, Value: {}", key, value);
    }

    /**
     * 키에 값 설정 (커스텀 TTL)
     * 
     * @param key Redis 키
     * @param value 저장할 값
     * @param timeout 만료 시간
     * @param unit 시간 단위
     */
    public void setValue(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        log.info("Redis SET with custom TTL - Key: {}, Value: {}, TTL: {} {}", key, value, timeout, unit);
    }

    /**
     * 키의 값 조회
     * 
     * @param key Redis 키
     * @return 저장된 값
     */
    public String getValue(String key) {
        String value = redisTemplate.opsForValue().get(key);
        log.debug("Redis GET - Key: {}, Value: {}", key, value);
        return value;
    }

    /**
     * 키의 만료 시간 설정
     * 
     * @param key Redis 키
     * @param timeout 만료 시간
     * @param unit 시간 단위
     * @return 설정 성공 여부
     */
    public Boolean setExpire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        log.info("Redis EXPIRE - Key: {}, TTL: {} {}, Result: {}", key, timeout, unit, result);
        return result;
    }

    /**
     * 키의 남은 만료 시간 조회 (초 단위)
     * 
     * @param key Redis 키
     * @return 남은 시간 (초), 키가 없으면 -2, 만료시간이 없으면 -1
     */
    public Long getExpire(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        log.debug("Redis TTL - Key: {}, TTL: {}s", key, ttl);
        return ttl;
    }

    /**
     * 키의 남은 만료 시간 조회 (지정된 시간 단위)
     * 
     * @param key Redis 키
     * @param unit 시간 단위
     * @return 남은 시간
     */
    public Long getExpire(String key, TimeUnit unit) {
        Long ttl = redisTemplate.getExpire(key, unit);
        log.debug("Redis TTL - Key: {}, TTL: {} {}", key, ttl, unit);
        return ttl;
    }

    /**
     * 키 삭제
     * 
     * @param key Redis 키
     * @return 삭제 성공 여부
     */
    public Boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        log.info("Redis DELETE - Key: {}, Result: {}", key, result);
        return result;
    }

    /**
     * 키가 존재하는지 확인
     * 
     * @param key Redis 키
     * @return 존재 여부
     */
    public Boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        log.debug("Redis EXISTS - Key: {}, Exists: {}", key, exists);
        return exists;
    }

    /**
     * 패턴과 일치하는 모든 키 조회
     * 
     * @param pattern 패턴 (예: "user:*")
     * @return 일치하는 키 목록
     */
    public Set<String> getKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        log.debug("Redis KEYS - Pattern: {}, Count: {}", pattern, keys != null ? keys.size() : 0);
        return keys;
    }
}


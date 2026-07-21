package com.oa.platform.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Redis 工具，基于 StringRedisTemplate。
 */
@Component
public class RedisUtil {

    private final StringRedisTemplate redis;

    public RedisUtil(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void set(String key, String value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redis.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    public Boolean expire(String key, Duration ttl) {
        return redis.expire(key, ttl);
    }

    public Set<String> keys(String pattern) {
        return redis.keys(pattern);
    }

    public long increment(String key, long delta) {
        return redis.opsForValue().increment(key, delta);
    }
}

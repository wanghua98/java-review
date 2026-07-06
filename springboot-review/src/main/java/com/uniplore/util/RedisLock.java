package com.uniplore.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * redis 分布式锁
 *
 * @author 杨锋
 */
@Component
@RequiredArgsConstructor
public class RedisLock {

    /**
     * 前缀
     */
    private final String prefix = "lock:";
    /**
     * redis 操作
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁
     *
     * @param key 加锁的键
     * @return 是否加锁成功
     */
    public boolean lock(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(prefix + key, "1", 10, TimeUnit.MINUTES));
    }

    /**
     * 解锁
     *
     * @param key 解锁的键
     */
    public void unlock(String key) {
        stringRedisTemplate.delete(prefix + key);
    }
}

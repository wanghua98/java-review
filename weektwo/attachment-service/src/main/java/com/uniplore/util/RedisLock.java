package com.uniplore.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁
 *
 * @author 杨锋
 */
@Component
@RequiredArgsConstructor
public class RedisLock {

    private final String prefix = "lock:";
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁（默认 10 秒自动释放）
     */
    public boolean lock(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate
                .opsForValue()
                .setIfAbsent(prefix + key, "1", 10, TimeUnit.SECONDS));
    }

    /**
     * 解锁
     */
    public void unlock(String key) {
        stringRedisTemplate.delete(prefix + key);
    }

    /**
     * 加锁（指定客户端标识，防止误删）
     */
    public boolean lock(String key, String clientId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(prefix + key, clientId, 10, TimeUnit.MINUTES)
        );
    }

    /**
     * 解锁（使用 Lua 脚本，只释放自己的锁）
     */
    public void unlock(String key, String clientId) {
        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        stringRedisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(prefix + key),
                clientId
        );
    }
}

package com.uniplore.springbootreview.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class CacheUtil {
    @Autowired
    private RedisLock redisLock;

    private final StringRedisTemplate stringRedisTemplate;

    public <T, R> R getCache(String prefix, T key, Function<T, R> function, Class<R> resultType, Long time, TimeUnit timeUnit) {
        String newKey = prefix + ":" + key;
        String s = stringRedisTemplate.opsForValue().get(newKey);
        //利用反序列化将json字符串转换为对象
        if (StrUtil.isNotBlank(s)) {
            return JSONUtil.toBean(s, resultType);
        }
        //不存在来查询数据库
        R result = function.apply(key);
        if (result == null) {

            return null;
        }
        set(newKey, result, time, timeUnit);
        return result;
    }

    private void set(String key, Object obj, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(obj), time, timeUnit);
    }


    /**
     * 互斥锁解决缓存击穿问题，并存储空值解决缓存穿透问题
     *
     */
    //private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    public <T, R> R getCacheWithMutex(String prefix, T key, Function<T, R> function, Class<R> resultType, Long time, TimeUnit timeUnit) {

        String newKey = prefix + ":" + key;
        String s = stringRedisTemplate.opsForValue().get(newKey);
        //利用反序列化将json字符串转换为对象
        if (StrUtil.isNotBlank(s)) {
            return JSONUtil.toBean(s, resultType);
        }
        //缓存中存在空值
        if (s != null) {
            return null;
        }

        R result;
        try {
            boolean isLock = redisLock.lock(newKey);
            // 如果获取锁失败，休眠并且重试
            if (!isLock) {
                Thread.sleep(500);
                return getCacheWithMutex(prefix, key, function, resultType, time, timeUnit);
            }

            result = function.apply(key);
            if (result == null) {
                set(newKey, "", time, timeUnit);
            }
            set(newKey, result, time, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            redisLock.unlock(newKey);
        }
        return result;
    }


}

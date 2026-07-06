package com.uniplore.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类
 *
 * @author 杨锋
 * @date 2026/07/06
 */
@RequiredArgsConstructor
@Component
public class CacheUtil {
    /**
     * redis锁
     */
    private final RedisLock redisLock;

    /**
     * redis模板
     */
    private final StringRedisTemplate stringRedisTemplate;

    public <T, R> R getCache(String prefix, T key, Function<T, R> function, Class<R> resultType, Long time, TimeUnit timeUnit) {
        //生成key
        String newKey = prefix + ":" + key;
        //从缓存中查询
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
        //将查询结果存储到缓存中
        set(newKey, result, time, timeUnit);
        return result;
    }

    /**
     * 将数据存储到缓存中
     */
    private void set(String key, Object obj, Long time, TimeUnit timeUnit) {
        //处理对象为空的情况
        if (obj == null) {
            obj = "";
        }
        if (time == null) {
            time = 10L;
            timeUnit = TimeUnit.MINUTES;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(obj), time, timeUnit);
    }


    /**
     * 互斥锁解决缓存击穿问题，并存储空值解决缓存穿透问题
     *
     */
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
            // 尝试获取锁
            boolean isLock = redisLock.lock(newKey);
            // 如果获取锁失败，休眠并且重试
            if (!isLock) {
                Thread.sleep(500);
                return getCacheWithMutex(prefix, key, function, resultType, time, timeUnit);
            }
            // 获取锁成功，执行查询操作
            result = function.apply(key);
            if (result == null) {
                // 存储空值
                set(newKey, "", time, timeUnit);
            }
            // 将查询结果存储到缓存中
            set(newKey, result, time, timeUnit);
        } catch (InterruptedException e) {
            // 处理中断异常
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            redisLock.unlock(newKey);
        }
        return result;
    }


}

package com.uniplore.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类
 * <p>
 * 提供 Redis 缓存通用操作，以及分片上传去重等专用方法。
 * </p>
 *
 * @author 杨锋
 */
@RequiredArgsConstructor
@Component
public class CacheUtil {

    private final RedisLock redisLock;
    private final StringRedisTemplate stringRedisTemplate;

    /** 分片上传记录的缓存前缀 */
    private static final String CHUNK_PREFIX = "chunk";

    /**
     * 判断分片是否已经上传过
     *
     * @param taskId      上传任务ID
     * @param chunkNumber 分片编号
     * @return true 表示已上传，false 表示未上传
     */
    public boolean isChunkUploaded(Long taskId, Integer chunkNumber) {
        String key = CHUNK_PREFIX + ":" + taskId + ":" + chunkNumber;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 标记分片已上传
     *
     * @param taskId      上传任务ID
     * @param chunkNumber 分片编号
     */
    public void markChunkUploaded(Long taskId, Integer chunkNumber) {
        String key = CHUNK_PREFIX + ":" + taskId + ":" + chunkNumber;
        stringRedisTemplate.opsForValue().set(key, "1", 1, TimeUnit.DAYS);
    }

    /**
     * 从缓存中获取数据，不存在则通过回调加载并回写缓存
     *
     * @param <T>        查询参数类型
     * @param <R>        返回结果类型
     * @param prefix     缓存键前缀
     * @param key        查询参数
     * @param function   未命中时加载数据的回调
     * @param resultType 结果类型（用于 JSON 反序列化）
     * @param time       过期时间
     * @param timeUnit   过期时间单位
     * @return 缓存数据
     */
    public <T, R> R getCache(String prefix, T key, Function<T, R> function,
                             Class<R> resultType, Long time, TimeUnit timeUnit) {
        String newKey = prefix + ":" + key;
        String s = stringRedisTemplate.opsForValue().get(newKey);

        if (StrUtil.isNotBlank(s)) {
            return JSONUtil.toBean(s, resultType);
        }

        R result = function.apply(key);
        if (result == null) {
            return null;
        }

        set(newKey, result, time, timeUnit);
        return result;
    }

    /**
     * 从缓存中获取数据（互斥锁防止缓存击穿）
     */
    public <T, R> R getCacheWithMutex(String prefix, T key, Function<T, R> function,
                                       Class<R> resultType, Long time, TimeUnit timeUnit) {
        String newKey = prefix + ":" + key;
        String s = stringRedisTemplate.opsForValue().get(newKey);

        if (StrUtil.isNotBlank(s)) {
            return JSONUtil.toBean(s, resultType);
        }
        if (s != null) {
            return null;
        }

        R result;
        try {
            boolean isLock = redisLock.lock(newKey);
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
            redisLock.unlock(newKey);
        }
        return result;
    }

    private void set(String key, Object obj, Long time, TimeUnit timeUnit) {
        if (obj == null) {
            obj = "";
        }
        if (time == null) {
            time = 10L;
            timeUnit = TimeUnit.MINUTES;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(obj), time, timeUnit);
    }
}

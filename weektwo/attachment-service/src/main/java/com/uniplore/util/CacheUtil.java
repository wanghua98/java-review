package com.uniplore.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.uniplore.pojo.FileChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * Redis 锁工具类，用于分片上传去重
     */
    private final RedisLock redisLock;
    /**
     * Redis 模板，用于字符串操作
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 分片上传任务缓存前缀（Hash 结构，一个任务一个 key）
     */
    private static final String CHUNK_TASK_PREFIX = "chunk:task";

    /**
     * 任务分片总数缓存后缀
     */
    private static final String CHUNK_TASK_COUNT_SUFFIX = ":cnt";

    /**
     * 判断分片是否已上传
     * <p>
     * 通过 Hash field 是否存在判断，O(1) 时间复杂度。
     * 一个上传任务只对应一个 Redis key。
     * </p>
     *
     * @param taskId      上传任务ID
     * @param chunkNumber 分片编号
     * @return true 表示已上传，false 表示未上传
     */
    public boolean isChunkUploaded(Long taskId, Integer chunkNumber) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(key, chunkNumber.toString()));
    }

    /**
     * 存储分片信息到 Redis Hash
     * <p>
     * 合并前只存 Redis，不写数据库；合并时再批量写入。
     * field 为分片编号，value 为 FileChunk 的 JSON。
     * </p>
     *
     * @param taskId 上传任务ID
     * @param chunk  分片信息
     */
    public void putChunkInfo(Long taskId, FileChunk chunk) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId;
        stringRedisTemplate.opsForHash().put(key, chunk.getChunkNumber().toString(), JSONUtil.toJsonStr(chunk));
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    /**
     * 获取某任务的全部分片信息
     * <p>
     * 合并前调用，从 Redis Hash 读出所有分片信息，用于批量写入数据库。
     * </p>
     *
     * @param taskId 上传任务ID
     * @return 分片信息列表
     */
    public List<FileChunk> getChunkInfos(Long taskId) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId;
        List<Object> values = stringRedisTemplate.opsForHash().values(key);
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return values.stream()
                .map(v -> JSONUtil.toBean(v.toString(), FileChunk.class))
                .collect(Collectors.toList());
    }

    /**
     * 删除某任务的全部分片缓存
     * <p>
     * 合并成功后调用，清理 Redis 中该任务的 Hash 和总数信息。
     * </p>
     *
     * @param taskId 上传任务ID
     */
    public void deleteChunkInfos(Long taskId) {
        String prefix = CHUNK_TASK_PREFIX + ":" + taskId;
        stringRedisTemplate.delete(prefix);
        stringRedisTemplate.delete(prefix + CHUNK_TASK_COUNT_SUFFIX);
    }

    /**
     * 存储任务分片总数到 Redis
     * <p>
     * initFile 时调用，供上传过程中通过 Redis 判断是否全部传完。
     * </p>
     *
     * @param taskId      上传任务ID
     * @param totalChunks 总分片数
     */
    public void putTaskMeta(Long taskId, Integer totalChunks) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId + CHUNK_TASK_COUNT_SUFFIX;
        stringRedisTemplate.opsForValue().set(key, totalChunks.toString(), 1, TimeUnit.DAYS);
    }

    /**
     * 获取任务分片总数
     *
     * @param taskId 上传任务ID
     * @return 总分片数，不存在返回 null
     */
    public Integer getTaskChunkCount(Long taskId) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId + CHUNK_TASK_COUNT_SUFFIX;
        String val = stringRedisTemplate.opsForValue().get(key);
        return val == null ? null : Integer.valueOf(val);
    }

    /**
     * 判断上传任务是否存在 Redis中是否有该任务的总数
     *
     * @param taskId 上传任务ID
     * @return true 表示任务存在
     */
    public boolean isTaskExists(Long taskId) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId + CHUNK_TASK_COUNT_SUFFIX;
        return stringRedisTemplate.opsForValue().get(key) != null;
    }

    /**
     * 获取已上传的分片数（Hash 的 field 数量）
     *
     * @param taskId 上传任务ID
     * @return 已上传分片数
     */
    public long getUploadedChunkCount(Long taskId) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId;
        Long count = stringRedisTemplate.opsForHash().size(key);
        return count == null ? 0L : count;
    }

    /**
     * 获取已上传的分片编号集合
     * <p>
     * 从 Redis Hash 的 field keys 读取，用于续传时告知前端哪些分片可跳过。
     * </p>
     *
     * @param taskId 上传任务ID
     * @return 已上传分片编号集合
     */
    public Set<Integer> getUploadedChunkNumbers(Long taskId) {
        String key = CHUNK_TASK_PREFIX + ":" + taskId;
        Set<Object> fields = stringRedisTemplate.opsForHash().keys(key);
        if (fields == null || fields.isEmpty()) {
            return new HashSet<>();
        }
        return fields.stream()
                .map(f -> Integer.valueOf(f.toString()))
                .collect(Collectors.toSet());
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

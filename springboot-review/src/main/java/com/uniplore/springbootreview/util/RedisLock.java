package com.uniplore.springbootreview.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLock {

    @Autowired
    private  StringRedisTemplate stringRedisTemplate;
    public  boolean lock(String key)
    {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10L, TimeUnit.SECONDS));
    }


    public  void unlock(String key)
    {
        stringRedisTemplate.delete(key);
    }
}

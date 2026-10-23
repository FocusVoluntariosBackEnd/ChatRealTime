package com.focustecnologia.chat_real_time.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DistributedLockService {
    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String lockKey, Duration timeout) {
         Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", timeout);

         return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }


}

package com.focustecnologia.chat_real_time.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimePublisher {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public void publish(RedisRealtimeEvent event) {
        try {
            redis.convertAndSend(RedisTopics.EVENTS, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not publish realtime event", exception);
        }
    }
}

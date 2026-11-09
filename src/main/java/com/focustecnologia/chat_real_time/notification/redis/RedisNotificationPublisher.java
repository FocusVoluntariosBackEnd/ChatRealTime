package com.focustecnologia.chat_real_time.notification.redis;

import com.focustecnologia.chat_real_time.notification.dto.websocket.NotificationNewPayload;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisNotificationPublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void transmitNotificationToUser(NotificationNewPayload payload) {
        WebSocketEvent<NotificationNewPayload> body = WebSocketEvent.toNotificationNew(payload);

        try {
            String json = objectMapper.writeValueAsString(body);
            String channel = "chat:users:" + payload.userId() + ":notifications";

            redisTemplate.convertAndSend(channel, json);
        } catch (JacksonException ex) {
            log.error("Failed to serialize message for Redis", ex);
            throw new IllegalStateException("Error serializing WebSocket event to Redis", ex);
        }
    }
}

package com.focustecnologia.chat_real_time.message.redis;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageNewPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageReadUpdatePayload;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void transmitMessage(MessageNewPayload payload) {
        WebSocketEvent<MessageNewPayload> body = WebSocketEvent.toMessageNew(payload);

        sendEventRedisChannel(body, payload.conversationId());
    }

    public void transmitMessageReadUpdate(MessageReadUpdatePayload payload) {
        WebSocketEvent<MessageReadUpdatePayload> body = WebSocketEvent.toMessageReadUpdate(payload);

        sendEventRedisChannel(body, payload.conversationId());
    }

    private void sendEventRedisChannel(WebSocketEvent<?> body, UUID conversationId) {
        try {
            String json = objectMapper.writeValueAsString(body);
            String channel = "chat:conversations:" + conversationId;

            redisTemplate.convertAndSend(channel, json);
        } catch (JacksonException ex) {
            log.error("Failed to serialize message for Redis", ex);
            throw new IllegalStateException("Error serializing WebSocket event to Redis", ex);
        }
    }
}

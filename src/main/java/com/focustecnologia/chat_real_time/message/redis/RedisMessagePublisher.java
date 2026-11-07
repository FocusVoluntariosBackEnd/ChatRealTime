package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageNewPayload;
import com.focustecnologia.chat_real_time.message.entity.Message;
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
public class RedisMessagePublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void transmitMessage(Message message) {
        MessageNewPayload payload = MessageNewPayload.toBody(message);
        WebSocketEvent<MessageNewPayload> body = WebSocketEvent.toNew(payload);

        try {
            String json = objectMapper.writeValueAsString(body);
            String channel = "chat:conversations:" + payload.conversationId();

            redisTemplate.convertAndSend(channel, json);
        } catch (JacksonException ex) {
            log.error("Failed to serialize message for Redis", ex);
            throw new IllegalStateException("Error serializing WebSocket event to Redis", ex);
        }
    }
}

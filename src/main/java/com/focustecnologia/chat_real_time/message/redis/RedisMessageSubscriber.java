package com.focustecnologia.chat_real_time.message.redis;

import com.focustecnologia.chat_real_time.config.RedisConfig;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {
    private static final String TOPIC_WEBSOCKET_CONVERSATIONS__PREFIX = "/topic/conversations/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String topicWebSocket = channel.replace(RedisConfig.CHAT_CONVERSATIONS_CHANNEL_PREFIX, TOPIC_WEBSOCKET_CONVERSATIONS__PREFIX);

        try {
            WebSocketEvent<?> event = objectMapper.readValue(json, WebSocketEvent.class);
            messagingTemplate.convertAndSend(topicWebSocket, event);
        } catch (JacksonException ex) {
            log.error("Failed to deserialize Redis message", ex);
        }
    }
}

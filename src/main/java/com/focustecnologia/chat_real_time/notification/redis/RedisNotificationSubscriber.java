package com.focustecnologia.chat_real_time.notification.redis;

import com.focustecnologia.chat_real_time.config.RedisConfig;
import com.focustecnologia.chat_real_time.notification.dto.websocket.NotificationNewPayload;
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
public class RedisNotificationSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final String USERS_NOTIFICATIONS_TOPIC = "/queue/notifications";

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        String userId = channel.split(":")[2];

        try {
            WebSocketEvent<NotificationNewPayload> event = objectMapper.readValue(json, WebSocketEvent.class);
            messagingTemplate.convertAndSendToUser(userId, USERS_NOTIFICATIONS_TOPIC, event);
        } catch (JacksonException ex) {
            log.error("Failed to deserialize Redis message", ex);
        }
    }
}

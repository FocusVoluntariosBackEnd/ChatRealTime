package com.focustecnologia.chat_real_time.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RealtimeRedisListener {
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final RealtimeDeliveryAuthorizer deliveryAuthorizer;

    @RedisListener(topic = RedisTopics.EVENTS, container = "realtimeRedisListenerContainer")
    public void receive(String json) {
        try {
            RedisRealtimeEvent event = objectMapper.readValue(json, RedisRealtimeEvent.class);

            for (String recipientIdValue : event.recipientIds()) {
                UUID recipientId = UUID.fromString(recipientIdValue);

                if (simpUserRegistry.getUser(recipientIdValue) == null
                        || !deliveryAuthorizer.canDeliver(event, recipientId)) {
                    continue;
                }

                messagingTemplate.convertAndSendToUser(
                        recipientIdValue,
                        "/queue/events",
                        new WebSocketEvent<>(event.event(), event.data())
                );
            }
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid Redis realtime event", exception);
        }
    }
}

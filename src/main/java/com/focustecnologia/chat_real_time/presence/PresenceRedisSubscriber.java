package com.focustecnologia.chat_real_time.presence;

import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceRedisSubscriber implements MessageListener {
    private static final String USER_PRESENCE_QUEUE = "/queue/presence";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final PresenceRecipientResolver recipientResolver;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            PresenceRedisEvent event = objectMapper.readValue(json, PresenceRedisEvent.class);
            UUID subjectUserId = event.payload().userId();

            for (String recipientIdValue : event.recipientIds()) {
                UUID recipientId = UUID.fromString(recipientIdValue);

                boolean connectedToThisInstance = simpUserRegistry.getUser(recipientIdValue) != null;
                boolean authorized = recipientResolver.canReceive(recipientId, subjectUserId);

                if (connectedToThisInstance && authorized) {
                    messagingTemplate.convertAndSendToUser(
                            recipientIdValue,
                            USER_PRESENCE_QUEUE,
                            new WebSocketEvent<>("user:presence", event.payload())
                    );
                }
            }
        } catch (JacksonException | IllegalArgumentException ex) {
            log.error("Could not process presence event from Redis.", ex);
        }
    }
}

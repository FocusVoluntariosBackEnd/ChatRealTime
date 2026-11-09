package com.focustecnologia.chat_real_time.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceRedisPublisher {
    public static final String CHANNEL = "chat:presence:events";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PresenceRecipientResolver recipientResolver;

    public void publish(UUID userId, PresenceStatus status) {
        List<String> recipientIds = recipientResolver.findRecipients(userId).stream()
                .map(UUID::toString)
                .toList();

        if (recipientIds.isEmpty()) {
            return;
        }

        PresenceRedisEvent event = new PresenceRedisEvent(
                new UserPresencePayload(userId, status),
                recipientIds
        );

        try {
            redisTemplate.convertAndSend(CHANNEL, objectMapper.writeValueAsString(event));
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not publish presence event.", ex);
        }
    }
}

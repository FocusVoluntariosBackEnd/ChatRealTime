package com.focustecnologia.chat_real_time.notification.dto.websocket;

import com.focustecnologia.chat_real_time.notification.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationNewPayload(
        UUID id,
        UUID conversationId,
        UUID messageId,
        UUID senderId,
        String content,
        UUID userId,
        Instant createdAt
) {
    public static NotificationNewPayload toBody(Notification notification) {
        return new NotificationNewPayload(
                notification.getId(),
                notification.getMessage().getConversation().getId(),
                notification.getMessage().getId(),
                notification.getMessage().getSender().getId(),
                notification.getMessage().getContent(),
                notification.getUser().getId(),
                notification.getCreatedAt()
        );
    }
}

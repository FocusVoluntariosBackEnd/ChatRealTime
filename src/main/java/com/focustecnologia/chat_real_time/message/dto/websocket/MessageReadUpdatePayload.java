package com.focustecnologia.chat_real_time.message.dto.websocket;

import com.focustecnologia.chat_real_time.message.entity.MessageRead;

import java.time.Instant;
import java.util.UUID;

public record MessageReadUpdatePayload (
        UUID conversationId,
        UUID messageId,
        UUID userId,
        Instant readAt
) {
    public static MessageReadUpdatePayload toBody(MessageRead read) {
        return new MessageReadUpdatePayload(
                read.getMessage().getConversation().getId(),
                read.getMessage().getId(),
                read.getUser().getId(),
                read.getReadAt()
        );
    }
}

package com.focustecnologia.chat_real_time.message.dto.websocket;

import com.focustecnologia.chat_real_time.message.entity.Message;

import java.time.Instant;
import java.util.UUID;

public record MessageNewPayload(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String senderName,
        String content,
        Instant createdAt
) {
    public static MessageNewPayload toBody(Message message) {
        return new MessageNewPayload(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}

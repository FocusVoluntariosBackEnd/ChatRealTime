package com.focustecnologia.chat_real_time.message.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageNewPayload(
        UUID messageId,
        UUID conversationId,
        UUID senderId,
        String content,
        Instant createdAt
) {
}

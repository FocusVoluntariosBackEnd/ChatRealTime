package com.focustecnologia.chat_real_time.conversation.dto;

import com.focustecnologia.chat_real_time.conversation.entity.Type;

import java.time.Instant;
import java.util.UUID;

public record ConversationSumaryResponse(
        UUID id,
        Type type,
        String displayName,
        Instant createdAt) {
}

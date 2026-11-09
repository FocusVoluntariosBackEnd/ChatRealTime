package com.focustecnologia.chat_real_time.message.dto.websocket;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageReadPayload(
        @NotNull
        UUID conversationId,
        @NotNull
        UUID messageId
) {}

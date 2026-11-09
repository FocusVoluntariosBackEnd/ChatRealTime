package com.focustecnologia.chat_real_time.message.dto.websocket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageSendPayload(
        @NotNull
        UUID conversationId,
        @NotBlank
        String clientMessageId,
        @NotBlank
        String content
) {
}

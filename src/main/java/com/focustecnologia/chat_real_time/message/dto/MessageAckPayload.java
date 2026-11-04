package com.focustecnologia.chat_real_time.message.dto;

import com.focustecnologia.chat_real_time.message.entity.Message;

import java.util.UUID;

public record MessageAckPayload(
        String clientMessageId,
        UUID messageId,
        String status
) {
    public static MessageAckPayload toBody(Message message) {
        return new MessageAckPayload(
                message.getClientMessageId(),
                message.getId(),
                "SAVED"
        );
    }
}

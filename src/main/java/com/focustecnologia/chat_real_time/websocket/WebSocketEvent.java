package com.focustecnologia.chat_real_time.websocket;

import com.focustecnologia.chat_real_time.message.dto.MessageAckPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebSocketEvent<T>(
        @NotBlank
        String event,
        @NotNull
        T data
) {
        public static WebSocketEvent<MessageAckPayload> toAck(MessageAckPayload payload) {
                return new WebSocketEvent<>("message:ack", payload);
        }
}

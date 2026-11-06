package com.focustecnologia.chat_real_time.websocket;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageNewPayload;
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

        public static WebSocketEvent<MessageNewPayload> toNew(MessageNewPayload payload) {
                return new WebSocketEvent<>("message:new", payload);
        }
}

package com.focustecnologia.chat_real_time.websocket;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageNewPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageReadUpdatePayload;
import com.focustecnologia.chat_real_time.notification.dto.websocket.NotificationNewPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebSocketEvent<T>(
        @NotBlank
        String event,
        @NotNull
        T data
) {
        public static WebSocketEvent<MessageAckPayload> toMessageAck(MessageAckPayload payload) {
                return new WebSocketEvent<>("message:ack", payload);
        }

        public static WebSocketEvent<MessageNewPayload> toMessageNew(MessageNewPayload payload) {
                return new WebSocketEvent<>("message:new", payload);
        }

        public static WebSocketEvent<MessageReadUpdatePayload> toMessageReadUpdate(MessageReadUpdatePayload payload) {
                return new WebSocketEvent<>("message:read:update", payload);
        }

        public static WebSocketEvent<NotificationNewPayload> toNotificationNew(NotificationNewPayload payload) {
                return new WebSocketEvent<>("notification:new", payload);
        }
}

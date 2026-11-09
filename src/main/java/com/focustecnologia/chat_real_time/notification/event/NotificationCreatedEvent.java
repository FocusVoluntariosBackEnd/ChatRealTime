package com.focustecnologia.chat_real_time.notification.event;

import com.focustecnologia.chat_real_time.notification.dto.websocket.NotificationNewPayload;

public record NotificationCreatedEvent(
        NotificationNewPayload payload
) {}

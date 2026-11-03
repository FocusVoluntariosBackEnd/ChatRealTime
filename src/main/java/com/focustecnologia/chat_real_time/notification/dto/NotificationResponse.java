package com.focustecnologia.chat_real_time.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID messageId,
        boolean isRead,
        Instant createdAt
) {}

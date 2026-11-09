package com.focustecnologia.chat_real_time.presence;

import java.util.UUID;

public record UserPresencePayload(
        UUID userId,
        PresenceStatus status
) {
}

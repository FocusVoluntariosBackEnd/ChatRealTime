package com.focustecnologia.chat_real_time.presence;

public record PresenceChangedEvent(
        String userId,
        PresenceStatus status
) {
}

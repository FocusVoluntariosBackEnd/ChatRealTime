package com.focustecnologia.chat_real_time.presence;

import java.util.List;

public record PresenceRedisEvent(
        UserPresencePayload payload,
        List<String> recipientIds
) {
}

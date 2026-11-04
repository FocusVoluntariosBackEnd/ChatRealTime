package com.focustecnologia.chat_real_time.realtime;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record RedisRealtimeEvent(
        String event,
        List<String> recipientIds,
        JsonNode data
) {
}

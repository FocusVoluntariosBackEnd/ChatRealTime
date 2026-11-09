package com.focustecnologia.chat_real_time.message.event;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageReadUpdatePayload;

public record MessageReadEvent(MessageReadUpdatePayload payload) {
}

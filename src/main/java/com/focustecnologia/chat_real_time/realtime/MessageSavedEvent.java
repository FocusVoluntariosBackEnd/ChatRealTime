package com.focustecnologia.chat_real_time.realtime;

import com.focustecnologia.chat_real_time.message.dto.MessageNewPayload;

import java.util.List;

public record MessageSavedEvent(
        MessageNewPayload message,
        List<String> recipientIds
) {
}

package com.focustecnologia.chat_real_time.message.dto;

import java.util.List;

public record MessageListResponse(
        List<MessageResponse> messages,
        Integer nextPage
) {}

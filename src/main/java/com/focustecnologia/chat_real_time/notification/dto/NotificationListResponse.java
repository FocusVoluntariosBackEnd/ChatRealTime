package com.focustecnologia.chat_real_time.notification.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> notifications,
        Integer nextPage
) {}

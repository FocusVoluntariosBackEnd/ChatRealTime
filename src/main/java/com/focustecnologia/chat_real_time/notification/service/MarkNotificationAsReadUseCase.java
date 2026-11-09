package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void execute(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found.")
                );

        notification.markAsRead();
    }
}

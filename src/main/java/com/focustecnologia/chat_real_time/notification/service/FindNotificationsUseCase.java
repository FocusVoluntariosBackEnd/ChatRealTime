package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.notification.dto.NotificationListResponse;
import com.focustecnologia.chat_real_time.notification.dto.NotificationResponse;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse execute(UUID userId, int page, int limit) {
        if (page < 1 || limit < 1 || limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be at least 1 and limit between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Slice<Notification> result = notificationRepository.findByUserId(userId, pageable);

        List<NotificationResponse> notifications = result.getContent().stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getMessage().getId(),
                        notification.isRead(),
                        notification.getCreatedAt()
                ))
                .toList();

        Integer nextPage = result.hasNext() ? page + 1 : null;

        return new NotificationListResponse(notifications, nextPage);
    }
}

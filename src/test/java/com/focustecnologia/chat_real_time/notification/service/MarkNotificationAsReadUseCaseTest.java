package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import com.focustecnologia.chat_real_time.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadUseCaseTest {
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private MarkNotificationAsReadUseCase useCase;

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when notification not found")
    void notificationNotFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId, notificationId));
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void shouldMarkNotificationAsReadSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        User user = new User("User", "user@gmail.com", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        Conversation conversation = new Conversation(Type.GROUP, user);
        Message message = new Message(conversation, user, "Hello", "client-1");

        Notification notification = new Notification(user, message);
        ReflectionTestUtils.setField(notification, "id", notificationId);
        assertFalse(notification.isRead());

        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));

        assertDoesNotThrow(() -> useCase.execute(userId, notificationId));

        assertTrue(notification.isRead());
    }
}

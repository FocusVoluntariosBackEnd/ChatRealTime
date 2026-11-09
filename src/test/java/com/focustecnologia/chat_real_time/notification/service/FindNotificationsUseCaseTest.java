package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.notification.dto.NotificationListResponse;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import com.focustecnologia.chat_real_time.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindNotificationsUseCaseTest {
    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private FindNotificationsUseCase useCase;

    @Test
    @DisplayName("Should throw ResponseStatusException.class when page or limit is invalid")
    void invalidPagination() {
        UUID userId = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, 0, 20));
        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, 1, 101));
        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, 1, 0));
    }

    @Test
    @DisplayName("Should find notifications successfully")
    void shouldFindNotificationsSuccessfully() {
        UUID userId = UUID.randomUUID();

        User user = new User("User", "user@gmail.com", "password");
        ReflectionTestUtils.setField(user, "id", userId);

        Conversation conversation = new Conversation(Type.GROUP, user);
        ReflectionTestUtils.setField(conversation, "id", UUID.randomUUID());

        UUID messageId = UUID.randomUUID();
        Message message = new Message(conversation, user, "Hello", "client-1");
        ReflectionTestUtils.setField(message, "id", messageId);

        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(user, message);
        ReflectionTestUtils.setField(notification, "id", notificationId);

        Slice<Notification> slice = new SliceImpl<>(List.of(notification));

        when(notificationRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(slice);

        NotificationListResponse response = useCase.execute(userId, 1, 20);

        assertNotNull(response);
        assertEquals(1, response.notifications().size());
        assertEquals(notificationId, response.notifications().get(0).id());
        assertEquals(messageId, response.notifications().get(0).messageId());
        assertFalse(response.notifications().get(0).isRead());
    }
}

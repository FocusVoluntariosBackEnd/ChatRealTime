package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.event.NotificationCreatedEvent;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import com.focustecnologia.chat_real_time.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAndDispatchNotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private CreateAndDispatchNotificationService service;

    @Test
    @DisplayName("Should dispatch notifications only to other conversation members and not to sender")
    void shouldDispatchNotificationsOnlyToOtherMembers() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        User sender = new User("Sender", "sender@gmail.com", "password");
        ReflectionTestUtils.setField(sender, "id", senderId);

        User recipient = new User("Recipient", "recipient@gmail.com", "password");
        ReflectionTestUtils.setField(recipient, "id", recipientId);

        Conversation conversation = new Conversation(Type.GROUP, sender);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        Message message = new Message(conversation, sender, "Hello", "client-1");
        ReflectionTestUtils.setField(message, "id", UUID.randomUUID());

        ConversationMember senderMember = new ConversationMember(conversation, sender, Role.ADMIN);
        ConversationMember recipientMember = new ConversationMember(conversation, recipient, Role.MEMBER);

        Notification notification = new Notification(recipient, message);
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());

        when(memberRepository.findAllByConversationId(conversationId)).thenReturn(List.of(senderMember, recipientMember));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        assertDoesNotThrow(() -> service.dispatchMessageNotifications(message, senderId));

        // Deve salvar notificação exatamente 1 vez (para o destinatário, não para o remetente)
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(eventPublisher, times(1)).publishEvent(any(NotificationCreatedEvent.class));
    }
}

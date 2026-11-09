package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ForbiddenException;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageSendPayload;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.event.MessageCreatedEvent;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
import com.focustecnologia.chat_real_time.notification.service.CreateAndDispatchNotificationService;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMessageUseCaseTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreateAndDispatchNotificationService dispatchNotificationService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private SendMessageUseCase useCase;

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when conversation not found")
    void conversationNotFound() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        MessageSendPayload payload = new MessageSendPayload(conversationId, "client-123", "Hello");

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(senderId, payload));
    }

    @Test
    @DisplayName("Should throw ForbiddenException.class when user not participating conversation")
    void senderNotParticipatingConversation() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        MessageSendPayload payload = new MessageSendPayload(conversationId, "client-123", "Hello");

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation conversation = new Conversation(Type.GROUP, creator);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, senderId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> useCase.execute(senderId, payload));
    }

    @Test
    @DisplayName("Should return existing ack when message already exists (Idempotency)")
    void returnExistingAckWhenMessageAlreadyExists() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String clientMessageId = "client-123";
        MessageSendPayload payload = new MessageSendPayload(conversationId, clientMessageId, "Hello");

        User sender = new User("Sender", "sender@gmail.com", "password");
        ReflectionTestUtils.setField(sender, "id", senderId);

        Conversation conversation = new Conversation(Type.GROUP, sender);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        UUID messageId = UUID.randomUUID();
        Message existingMessage = new Message(conversation, sender, "Hello", clientMessageId);
        ReflectionTestUtils.setField(existingMessage, "id", messageId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, senderId)).thenReturn(true);
        when(userRepository.getReferenceById(senderId)).thenReturn(sender);
        when(messageRepository.findByConversationIdAndClientMessageId(conversationId, clientMessageId))
                .thenReturn(Optional.of(existingMessage));

        MessageAckPayload ack = useCase.execute(senderId, payload);

        assertNotNull(ack);
        assertEquals(clientMessageId, ack.clientMessageId());
        assertEquals(messageId, ack.messageId());
        assertEquals("SAVED", ack.status());

        verify(messageRepository, never()).saveAndFlush(any());
        verifyNoInteractions(dispatchNotificationService, eventPublisher);
    }

    @Test
    @DisplayName("Should send message successfully")
    void shouldSendMessageSuccessfully() {
        UUID senderId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        String clientMessageId = "client-123";
        MessageSendPayload payload = new MessageSendPayload(conversationId, clientMessageId, "Hello World");

        User sender = new User("Sender", "sender@gmail.com", "password");
        ReflectionTestUtils.setField(sender, "id", senderId);

        Conversation conversation = new Conversation(Type.GROUP, sender);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        UUID messageId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, senderId)).thenReturn(true);
        when(userRepository.getReferenceById(senderId)).thenReturn(sender);
        when(messageRepository.findByConversationIdAndClientMessageId(conversationId, clientMessageId))
                .thenReturn(Optional.empty());
        when(messageRepository.saveAndFlush(any(Message.class))).thenAnswer(invocation -> {
            Message m = invocation.getArgument(0);
            ReflectionTestUtils.setField(m, "id", messageId);
            return m;
        });

        MessageAckPayload ack = useCase.execute(senderId, payload);

        assertNotNull(ack);
        assertEquals(clientMessageId, ack.clientMessageId());
        assertEquals(messageId, ack.messageId());
        assertEquals("SAVED", ack.status());

        verify(messageRepository).saveAndFlush(any(Message.class));
        verify(eventPublisher).publishEvent(any(MessageCreatedEvent.class));
        verify(dispatchNotificationService).dispatchMessageNotifications(any(Message.class), eq(senderId));
    }
}

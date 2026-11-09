package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.entity.MessageRead;
import com.focustecnologia.chat_real_time.message.event.MessageReadEvent;
import com.focustecnologia.chat_real_time.message.repository.MessageReadRepository;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
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
class MarkMessageAsReadUseCaseTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageReadRepository messageReadRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private MarkMessageAsReadUseCase useCase;

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when message not found")
    void messageNotFound() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId, messageId));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when user not participating conversation")
    void memberNotFound() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation conversation = new Conversation(Type.GROUP, creator);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        Message message = new Message(conversation, creator, "Hello", "client-1");
        ReflectionTestUtils.setField(message, "id", messageId);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId, messageId));
    }

    @Test
    @DisplayName("Should return and do nothing when message is already read (Idempotency)")
    void returnWhenMessageAlreadyRead() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation conversation = new Conversation(Type.GROUP, creator);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        Message message = new Message(conversation, creator, "Hello", "client-1");
        ReflectionTestUtils.setField(message, "id", messageId);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(true);
        when(messageReadRepository.existsByMessageIdAndUserId(messageId, userId)).thenReturn(true);

        assertDoesNotThrow(() -> useCase.execute(userId, messageId));

        verify(messageReadRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should mark message as read successfully")
    void shouldMarkMessageAsReadSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        User reader = new User("Reader", "reader@gmail.com", "password");
        ReflectionTestUtils.setField(reader, "id", userId);

        Conversation conversation = new Conversation(Type.GROUP, reader);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        Message message = new Message(conversation, reader, "Hello", "client-1");
        ReflectionTestUtils.setField(message, "id", messageId);

        MessageRead savedRead = new MessageRead(message, reader);
        ReflectionTestUtils.setField(savedRead, "id", UUID.randomUUID());

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(true);
        when(messageReadRepository.existsByMessageIdAndUserId(messageId, userId)).thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(reader);
        when(messageReadRepository.save(any(MessageRead.class))).thenReturn(savedRead);

        assertDoesNotThrow(() -> useCase.execute(userId, messageId));

        verify(messageReadRepository).save(any(MessageRead.class));
        verify(eventPublisher).publishEvent(any(MessageReadEvent.class));
    }
}

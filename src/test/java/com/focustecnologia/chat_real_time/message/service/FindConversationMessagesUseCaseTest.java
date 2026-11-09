package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.dto.MessageListResponse;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
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
class FindConversationMessagesUseCaseTest {
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private MessageRepository messageRepository;
    @InjectMocks
    private FindConversationMessagesUseCase useCase;

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when user not member of conversation")
    void conversationNotFound() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(userId, conversationId, 1, 20));
    }

    @Test
    @DisplayName("Should throw ResponseStatusException.class when page or limit is invalid")
    void invalidPagination() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, conversationId, 0, 20));
        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, conversationId, 1, 101));
        assertThrows(ResponseStatusException.class, () -> useCase.execute(userId, conversationId, 1, 0));
    }

    @Test
    @DisplayName("Should find conversation messages successfully")
    void shouldFindMessagesSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        User sender = new User("Sender", "sender@gmail.com", "password");
        ReflectionTestUtils.setField(sender, "id", userId);

        Conversation conversation = new Conversation(Type.GROUP, sender);
        ReflectionTestUtils.setField(conversation, "id", conversationId);

        UUID messageId = UUID.randomUUID();
        Message message = new Message(conversation, sender, "Test Message", "client-1");
        ReflectionTestUtils.setField(message, "id", messageId);

        Slice<Message> messageSlice = new SliceImpl<>(List.of(message));

        when(memberRepository.existsByConversationIdAndUserId(conversationId, userId)).thenReturn(true);
        when(messageRepository.findByConversationId(eq(conversationId), any(Pageable.class))).thenReturn(messageSlice);

        MessageListResponse response = useCase.execute(userId, conversationId, 1, 20);

        assertNotNull(response);
        assertEquals(1, response.messages().size());
        assertEquals(messageId, response.messages().get(0).id());
        assertEquals("Test Message", response.messages().get(0).content());
        assertNull(response.nextPage()); // Apenas 1 página, não tem next
    }
}

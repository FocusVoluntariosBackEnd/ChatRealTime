package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.ConversationSumaryResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindAllConversationsUseCaseTest {
    @Mock
    private ConversationRepository conversationRepository;
    @InjectMocks
    private FindAllConversationsUseCase useCase;

    @Test
    @DisplayName("Should find all conversations for user successfully")
    void shouldFindAllConversationsSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        ConversationSumaryResponse summary = new ConversationSumaryResponse(
                conversationId,
                Type.GROUP,
                "Group Test",
                Instant.now()
        );
        Page<ConversationSumaryResponse> expectedPage = new PageImpl<>(List.of(summary));

        when(conversationRepository.findAllForUser(eq(userId), any(Pageable.class))).thenReturn(expectedPage);

        Page<ConversationSumaryResponse> response = useCase.execute(userId, 1, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(conversationId, response.getContent().get(0).id());
        assertEquals("Group Test", response.getContent().get(0).displayName());
    }
}

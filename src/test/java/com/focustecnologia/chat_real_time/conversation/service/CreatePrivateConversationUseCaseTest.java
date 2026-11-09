package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.exception.SameUsersConversationException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePrivateConversationUseCaseTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository conversationMemberRepository;
    @InjectMocks
    private CreatePrivateConversationUseCase useCase;

    @Test
    @DisplayName("Should throw SameUsersConversationException.class when creator is equals participant")
    void creatorIsEqualsParticipant() {
        UUID creatorId = UUID.randomUUID();
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(creatorId);

        assertThrows(SameUsersConversationException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when creator user not found")
    void creatorNotFound() {
        UUID creatorId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(participantId);

        when(userRepository.findById(creatorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when participant user not found")
    void participantNotFound() {
        UUID creatorId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(participantId);

        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", creatorId);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should return existing conversation when already exists")
    void returnExistingConversationWhenAlreadyExists() {
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID participantId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(participantId);

        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", creatorId);

        User participant = new User("Participant", "participant@gmail.com", "password");
        ReflectionTestUtils.setField(participant, "id", participantId);

        UUID conversationId = UUID.randomUUID();
        Conversation existingConversation = Conversation.createPrivate(creator, creator, participant);
        ReflectionTestUtils.setField(existingConversation, "id", conversationId);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(conversationRepository.findPrivateBetween(creatorId, participantId)).thenReturn(Optional.of(existingConversation));

        PrivateConversationResponse response = useCase.execute(creatorId, request);

        assertNotNull(response);
        assertEquals(conversationId, response.id());
        assertEquals(Type.PRIVATE, response.type());
        assertEquals(participant.getName(), response.participant().name());
        assertEquals(participant.getEmail(), response.participant().email());
    }

    @Test
    @DisplayName("Should create private conversation successfully")
    void createPrivateConversationSuccessfully() {
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID participantId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(participantId);

        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", creatorId);

        User participant = new User("Participant", "participant@gmail.com", "password");
        ReflectionTestUtils.setField(participant, "id", participantId);

        UUID conversationId = UUID.randomUUID();

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(conversationRepository.findPrivateBetween(creatorId, participantId)).thenReturn(Optional.empty());
        when(conversationRepository.saveAndFlush(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", conversationId);
            return c;
        });

        PrivateConversationResponse response = useCase.execute(creatorId, request);

        assertNotNull(response);
        assertEquals(conversationId, response.id());
        assertEquals(Type.PRIVATE, response.type());
        assertEquals(participant.getName(), response.participant().name());
        assertEquals(participant.getEmail(), response.participant().email());
    }

    @Test
    @DisplayName("Should handle race condition and return existing conversation when DataIntegrityViolationException occurs")
    void handleRaceConditionWhenDataIntegrityViolationOccurs() {
        UUID creatorId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID participantId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        CreatePrivateConversationRequest request = new CreatePrivateConversationRequest(participantId);

        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", creatorId);

        User participant = new User("Participant", "participant@gmail.com", "password");
        ReflectionTestUtils.setField(participant, "id", participantId);

        UUID concurrentConversationId = UUID.randomUUID();
        Conversation concurrentConversation = Conversation.createPrivate(participant, creator, participant);
        ReflectionTestUtils.setField(concurrentConversation, "id", concurrentConversationId);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(conversationRepository.findPrivateBetween(creatorId, participantId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(concurrentConversation));

        when(conversationRepository.saveAndFlush(any(Conversation.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        PrivateConversationResponse response = useCase.execute(creatorId, request);

        assertNotNull(response);
        assertEquals(concurrentConversationId, response.id());
        assertEquals(participant.getName(), response.participant().name());
    }
}

package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreateGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.GroupResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateGroupUseCaseTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CreateGroupUseCase useCase;

    @Test
    @DisplayName("Should throw SameUsersConversationException.class when creator belongs group list members")
    void creatorBelongsGroupListMembers() {
        UUID creatorId = UUID.randomUUID();
        CreateGroupRequest request = new CreateGroupRequest("Test Group", List.of(creatorId, UUID.randomUUID()));

        assertThrows(SameUsersConversationException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when creator user is not found")
    void creatorNotFound() {
        UUID creatorId = UUID.randomUUID();
        CreateGroupRequest request = new CreateGroupRequest("Test Group", List.of(UUID.randomUUID()));

        when(userRepository.findById(creatorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when one or more participant users not found")
    void OneOrMoreParticipantsNotFound() {
        UUID member1Id = UUID.randomUUID();
        UUID member2Id = UUID.randomUUID();
        CreateGroupRequest request = new CreateGroupRequest("Test Group", List.of(member1Id, member2Id));

        User member1 = new User("Member 1", "member1@gmail.com", "password");

        UUID creatorId = UUID.randomUUID();
        User creator = new User("Creator", "creator@gmail.com", "password");

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(any())).thenReturn(List.of(member1));

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(creatorId, request));
    }

    @Test
    @DisplayName("Should create group successfully")
    void shouldCreateGroupSuccessfully() {
        UUID memberUser1Id = UUID.randomUUID();
        UUID memberUser2Id = UUID.randomUUID();
        CreateGroupRequest request = new CreateGroupRequest("Test Group", List.of(memberUser1Id, memberUser2Id));

        User memberUser1 = new User("Member 1", "user1@gmail.com", "password");
        ReflectionTestUtils.setField(memberUser1, "id", memberUser1Id);
        User memberUser2 = new User("Member 2", "user2@gmail.com", "password");
        ReflectionTestUtils.setField(memberUser2, "id", memberUser2Id);

        UUID creatorId = UUID.randomUUID();
        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", creatorId);

        UUID groupId = UUID.randomUUID();
        Conversation group = Conversation.createGroup(creator, request.name());
        ReflectionTestUtils.setField(group, "id", groupId);

        ConversationMember adminMember = ConversationMember.createAdmin(group, creator);
        ReflectionTestUtils.setField(adminMember, "id", UUID.randomUUID());

        UUID member1Id = UUID.randomUUID();
        ConversationMember member1 = ConversationMember.createMember(group, memberUser1);
        ReflectionTestUtils.setField(member1, "id", member1Id);

        UUID member2Id = UUID.randomUUID();
        ConversationMember member2 = ConversationMember.createMember(group, memberUser2);
        ReflectionTestUtils.setField(member2, "id", member2Id);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(any())).thenReturn(List.of(memberUser1, memberUser2));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(group);
        when(memberRepository.saveAll(anyList())).thenReturn(List.of(adminMember, member1, member2));

        GroupResponse response = useCase.execute(creatorId, request);

        assertNotNull(response);
        assertEquals(groupId, response.id());
        assertEquals("Test Group", response.name());
        assertEquals(3, response.members().size());
    }
}

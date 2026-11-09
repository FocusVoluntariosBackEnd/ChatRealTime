package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ForbiddenException;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.exception.SameUsersConversationException;
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
class DeleteMemberGroupUseCaseTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @InjectMocks
    private DeleteMemberGroupUseCase useCase;

    @Test
    @DisplayName("Should throw SameUsersConversationException.class when member to be deleted is equals admin")
    void memberIsEqualsAdmin() {
        UUID adminId = UUID.randomUUID();

        assertThrows(SameUsersConversationException.class, () -> useCase.execute(adminId, UUID.randomUUID(), adminId));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when group not found")
    void groupNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(adminId, groupId, memberId));
    }

    @Test
    @DisplayName("Should throw ForbiddenException.class when adminId not represent admin conversation")
    void adminIdNotRepresentAdminConversation() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation group = Conversation.createGroup(creator, "Group Test");

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> useCase.execute(adminId, groupId, memberId));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when member to be removed not found in group")
    void memberNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation group = Conversation.createGroup(creator, "Group Test");

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(groupId, memberId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(adminId, groupId, memberId));
    }

    @Test
    @DisplayName("Should delete member from group successfully")
    void shouldDeleteMemberSuccessfully() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        User creator = new User("Creator", "creator@gmail.com", "password");
        Conversation group = Conversation.createGroup(creator, "Group Test");

        User memberUser = new User("Member", "member@gmail.com", "password");
        ReflectionTestUtils.setField(memberUser, "id", memberId);

        ConversationMember member = ConversationMember.createMember(group, memberUser);

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(true);
        when(memberRepository.findByConversationIdAndUserId(groupId, memberId)).thenReturn(Optional.of(member));

        assertDoesNotThrow(() -> useCase.execute(adminId, groupId, memberId));

        verify(memberRepository).delete(member);
    }
}

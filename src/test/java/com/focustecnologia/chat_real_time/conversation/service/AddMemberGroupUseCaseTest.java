package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.AddMemberGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.ConversationMemberResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ForbiddenException;
import com.focustecnologia.chat_real_time.exception.MemberAlreadyInGroupException;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddMemberGroupUseCaseTest {
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AddMemberGroupUseCase useCase;

    @Test
    @DisplayName("Should throw SameUsersConversationException.class when member to be added is equals admin")
    void memberIsEqualsAdmin() {
        UUID adminId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(adminId);

        assertThrows(SameUsersConversationException.class, () -> useCase.execute(adminId, UUID.randomUUID(), request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException.class when group not found")
    void groupNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(memberId);
        UUID groupId = UUID.randomUUID();

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(adminId, groupId, request));
    }

    @Test
    @DisplayName("Should throw ForbiddenException.class when adminId not represent admin conversation")
    void adminIdNotRepresentAdminConversation() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(memberId);

        User creator = new User("Creator", "creator@gmail.com", "password");

        UUID groupId = UUID.randomUUID();
        Conversation group = Conversation.createGroup(creator, "Group Test");

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> useCase.execute(adminId, groupId, request));
    }

    @Test
    @DisplayName("Should throw MemberAlreadyInGroup.class when member to be added already exists in group")
    void memberAlreadyInGroup() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(memberId);

        User creator = new User("Creator", "creator@gmail.com", "password");

        UUID groupId = UUID.randomUUID();
        Conversation group = Conversation.createGroup(creator, "Group Test");

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(groupId, memberId)).thenReturn(true);

        assertThrows(MemberAlreadyInGroupException.class, () -> useCase.execute(adminId, groupId, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFound.class when member user not found")
    void memberUserNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(memberId);

        User creator = new User("Creator", "creator@gmail.com", "password");

        UUID groupId = UUID.randomUUID();
        Conversation group = Conversation.createGroup(creator, "Group Test");

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(groupId, memberId)).thenReturn(false);
        when(userRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(adminId, groupId, request));

    }

    @Test
    @DisplayName("Should add member in group successfully")
    void addMemberInGroupSuccessfully() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        AddMemberGroupRequest request = new AddMemberGroupRequest(memberId);

        User member = new User("Member", "member@gmail.com", "password");
        ReflectionTestUtils.setField(member, "id", memberId);

        User creator = new User("Creator", "creator@gmail.com", "password");
        ReflectionTestUtils.setField(creator, "id", adminId);

        UUID groupId = UUID.randomUUID();
        Conversation group = Conversation.createGroup(creator, "Group Test");
        ReflectionTestUtils.setField(group, "id", groupId);

        UUID conversationMemberId = UUID.randomUUID();
        ConversationMember conversationMember = ConversationMember.createMember(group, member);
        ReflectionTestUtils.setField(conversationMember, "id", conversationMemberId);

        when(conversationRepository.findByIdAndType(groupId, Type.GROUP)).thenReturn(Optional.of(group));
        when(memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(groupId, memberId)).thenReturn(false);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(ConversationMember.class))).thenReturn(conversationMember);

        ConversationMemberResponse response = useCase.execute(adminId, groupId, request);

        assertEquals(response.id(), conversationMember.getId());
        assertEquals(Role.MEMBER, response.role());
        assertEquals(response.userId(), member.getId());
        assertEquals(response.userEmail(), member.getEmail());
        assertEquals(response.userName(), member.getName());
    }
}
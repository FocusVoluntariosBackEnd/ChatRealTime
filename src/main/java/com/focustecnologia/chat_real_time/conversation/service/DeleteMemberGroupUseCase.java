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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteMemberGroupUseCase {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;

    @Transactional
    public void execute(UUID adminId, UUID groupId, UUID memberId) {
        validateDistinctUsers(adminId, memberId);

        Conversation group = conversationRepository.findByIdAndType(groupId, Type.GROUP)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        validateAdminPermission(groupId, adminId);

        ConversationMember memberDeleted = memberRepository.findByConversationIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        memberRepository.delete(memberDeleted);
    }

    private void validateDistinctUsers(UUID userAId, UUID userBId) {
        if (userAId.equals(userBId)) {
            throw new SameUsersConversationException("Cannot remove yourself from the group.");
        }
    }

    private void validateAdminPermission(UUID groupId, UUID adminId) {
        boolean isAdmin = memberRepository.existsByConversationIdAndUserIdAndRole(groupId, adminId, Role.ADMIN);
        if (!isAdmin) {
            throw new ForbiddenException("Only admins can remove members from this group.");
        }
    }
}

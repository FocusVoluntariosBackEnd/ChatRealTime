package com.focustecnologia.chat_real_time.conversation.dto;

import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;

import java.util.UUID;

public record ConversationMemberResponse (
        UUID id,
        UUID userId,
        String userName,
        String userEmail,
        Role role
){
    public static ConversationMemberResponse toResponse(ConversationMember member) {
        return new ConversationMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getEmail(),
                member.getRole()
        );
    }
}

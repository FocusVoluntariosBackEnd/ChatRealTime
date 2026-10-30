package com.focustecnologia.chat_real_time.conversation.dto;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Type;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        Type type,
        Instant createdAt,
        List<ConversationMemberResponse> members
) {
    public static GroupResponse toResponse(Conversation conversation, List<ConversationMember> members) {
        return new GroupResponse(
                conversation.getId(),
                conversation.getName(),
                conversation.getType(),
                conversation.getCreatedAt(),
                toMembersResponse(members)
        );
    }

    private static List<ConversationMemberResponse> toMembersResponse(List<ConversationMember> members) {
        return members.stream()
                .map(ConversationMemberResponse::toResponse)
                .toList();
    }
}

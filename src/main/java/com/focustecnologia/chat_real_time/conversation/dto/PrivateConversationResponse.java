package com.focustecnologia.chat_real_time.conversation.dto;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.user.dto.ParticipantResponse;
import com.focustecnologia.chat_real_time.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public record PrivateConversationResponse (
        UUID id,
        Type type,
        Instant createdAt,
        ParticipantResponse participant
) {
    public static PrivateConversationResponse toResponse(Conversation conversation, User participant) {
        return new PrivateConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getCreatedAt(),
                new ParticipantResponse(
                        participant.getId(),
                        participant.getName(),
                        participant.getEmail()
                )
        );
    }
}

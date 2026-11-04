package com.focustecnologia.chat_real_time.realtime;

import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RealtimeDeliveryAuthorizer {
    private final ConversationMemberRepository memberRepository;

    public boolean canDeliver(RedisRealtimeEvent event, UUID recipientId) {
        return switch (event.event()) {
            case "message:new" -> canReceiveMessage(event, recipientId);
            case "user:presence" -> canReceivePresence(event, recipientId);
            default -> true;
        };
    }

    private boolean canReceiveMessage(RedisRealtimeEvent event, UUID recipientId) {
        String conversationIdValue = event.data().path("conversationId").asText();

        if (conversationIdValue.isBlank()) {
            return false;
        }

        try {
            UUID conversationId = UUID.fromString(conversationIdValue);

            return memberRepository.existsByConversationIdAndUserId(conversationId, recipientId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean canReceivePresence(RedisRealtimeEvent event, UUID recipientId) {
        String subjectUserIdValue = event.data().path("userId").asText();

        if (subjectUserIdValue.isBlank()) {
            return false;
        }

        try {
            UUID subjectUserId = UUID.fromString(subjectUserIdValue);

            return memberRepository.countSharedConversations(recipientId, subjectUserId) > 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}

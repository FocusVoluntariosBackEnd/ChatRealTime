package com.focustecnologia.chat_real_time.presence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.realtime.RedisRealtimeEvent;
import com.focustecnologia.chat_real_time.realtime.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PublishPresenceChanged {
    private final ConversationMemberRepository memberRepository;
    private final RealtimePublisher realtimePublisher;
    private final ObjectMapper objectMapper;

    @EventListener
    public void publish(PresenceChangedEvent event) {
        UUID userId = UUID.fromString(event.userId());

        List<String> recipientIds = memberRepository
                .findDistinctUserIdsSharingConversationWith(userId)
                .stream()
                .map(UUID::toString)
                .toList();

        if (recipientIds.isEmpty()) {
            return;
        }

        realtimePublisher.publish(new RedisRealtimeEvent(
                "user:presence",
                recipientIds,
                objectMapper.valueToTree(new UserPresencePayload(userId, event.status()))
        ));
    }
}

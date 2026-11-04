package com.focustecnologia.chat_real_time.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PublishMessageAfterCommit {
    private final RealtimePublisher realtimePublisher;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(MessageSavedEvent event) {
        realtimePublisher.publish(new RedisRealtimeEvent(
                "message:new",
                event.recipientIds(),
                objectMapper.valueToTree(event.message())
        ));
    }
}

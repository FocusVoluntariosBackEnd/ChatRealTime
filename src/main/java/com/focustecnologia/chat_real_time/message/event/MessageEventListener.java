package com.focustecnologia.chat_real_time.message.event;

import com.focustecnologia.chat_real_time.message.redis.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {
    private final RedisMessagePublisher messagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageCreated(MessageCreatedEvent event) {
        messagePublisher.transmitMessage(event.payload());

        log.info(
                "Publishing message after transaction commit. messageId={}",
                event.payload().id()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageRead(MessageReadEvent event) {
        messagePublisher.transmitMessageReadUpdate(event.payload());

        log.info(
                "Publishing message read after transaction commit. messageId={}",
                event.payload().messageId()
        );
    }
}

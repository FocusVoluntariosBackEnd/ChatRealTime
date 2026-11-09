package com.focustecnologia.chat_real_time.notification.event;

import com.focustecnologia.chat_real_time.notification.redis.RedisNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final RedisNotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        notificationPublisher.transmitNotificationToUser(event.payload());

        log.info("Publishing notification after transaction commit. notificationId={}",
                event.payload().id());
    }
}

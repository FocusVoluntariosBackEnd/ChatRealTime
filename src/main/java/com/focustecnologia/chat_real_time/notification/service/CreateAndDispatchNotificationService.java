package com.focustecnologia.chat_real_time.notification.service;

import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.notification.dto.websocket.NotificationNewPayload;
import com.focustecnologia.chat_real_time.notification.entity.Notification;
import com.focustecnologia.chat_real_time.notification.event.NotificationCreatedEvent;
import com.focustecnologia.chat_real_time.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAndDispatchNotificationService {
    private final NotificationRepository notificationRepository;
    private final ConversationMemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void dispatchMessageNotifications(Message message, UUID senderId) {
        List<ConversationMember> members = memberRepository.findAllByConversationId(message.getConversation().getId());

        for (ConversationMember member : members) {
            if (!member.getUser().getId().equals(senderId)) {
                Notification notification = notificationRepository
                        .save(new Notification(member.getUser(), message));

                NotificationNewPayload payload = NotificationNewPayload.toBody(notification);

                eventPublisher.publishEvent(new NotificationCreatedEvent(payload));
            }
        }
    }
}

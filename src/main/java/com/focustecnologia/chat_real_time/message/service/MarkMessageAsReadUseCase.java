package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageReadUpdatePayload;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.entity.MessageRead;
import com.focustecnologia.chat_real_time.message.event.MessageReadEvent;
import com.focustecnologia.chat_real_time.message.repository.MessageReadRepository;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkMessageAsReadUseCase {
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID userId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found."));

        UUID conversationId = message.getConversation().getId();

        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ResourceNotFoundException("Member not found.");
        }

        if (messageReadRepository.existsByMessageIdAndUserId(messageId, userId)) {
            return;
        }

        MessageRead read = messageReadRepository.save(new MessageRead(message, userRepository.getReferenceById(userId)));

        MessageReadUpdatePayload payload = MessageReadUpdatePayload.toBody(read);

        eventPublisher.publishEvent(new MessageReadEvent(payload));
    }
}

package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ForbiddenException;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageSendPayload;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendMessageUseCase {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageAckPayload execute(UUID senderId, MessageSendPayload payload) {
        Conversation conversation = conversationRepository.findById(payload.conversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        validateExistentMember(conversation.getId(), senderId);

        User sender = userRepository.getReferenceById(senderId);

        Optional<Message> message = messageRepository.findByConversationIdAndClientMessageId(conversation.getId(), payload.clientMessageId());

        if (message.isPresent()) {
            return MessageAckPayload.toBody(message.get());
        }

        Message newMessage = createMessage(conversation, sender, payload.content(), payload.clientMessageId());

        return MessageAckPayload.toBody(newMessage);
    }

    private void validateExistentMember(UUID conversationId, UUID senderId) {
        boolean exists = memberRepository.existsByConversationIdAndUserId(conversationId, senderId);

        if(!exists) {
            throw new ForbiddenException("User not participating conversation to send message");
        }
    }

    private Message createMessage(Conversation conversation, User user, String content, String clientMessageId) {
        Message message = new Message(conversation, user, content, clientMessageId);
        return messageRepository.save(message);
    }
}

package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.message.dto.MessageListResponse;
import com.focustecnologia.chat_real_time.message.dto.MessageResponse;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindConversationMessagesUseCase {
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public MessageListResponse execute(UUID userId, UUID conversationId, int page, int limit) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ResourceNotFoundException("Conversation not found.");
        }

        if (page < 1 || limit < 1 || limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be at least 1 and limit between 1 and 100."
            );
        }

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Slice<Message> result = messageRepository.findByConversationId(conversationId, pageable);

        List<MessageResponse> messages = result.getContent().stream()
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getSender().getId(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .toList();

        Integer nextPage = result.hasNext() ? page + 1 : null;

        return new MessageListResponse(messages, nextPage);
    }
}

package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.ConversationSumaryResponse;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindAllConversationsUseCase {
    private final ConversationRepository conversationRepository;

    public Page<ConversationSumaryResponse> execute(UUID id, int page, int size) {
        page = Math.max(0, page - 1);
        size = Math.min(Math.max(1, size), 25);

        Pageable pageable = PageRequest.of(page, size);

        return conversationRepository.findAllForUser(id, pageable);

    }

}

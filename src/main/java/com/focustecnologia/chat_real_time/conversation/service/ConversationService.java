package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public PrivateConversationResponse createPrivate(User authenticatedUser, CreatePrivateConversationRequest request) {
        UUID authenticatedUserId = authenticatedUser.getId();
        UUID participantUserId = request.participantId();

        if (authenticatedUserId.equals(participantUserId)) {
            throw new IllegalArgumentException("Not allowed start private conversation with yourself.");
        }

        User participantUser = userRepository.findById(participantUserId)
                .orElseThrow(() -> new RuntimeException("Participant not found."));

        Optional<Conversation> existingConversation = conversationMemberRepository
                .findPrivateConversationBetweenUsers(authenticatedUserId, participantUserId);

        if (existingConversation.isPresent()) {
            return PrivateConversationResponse.toResponse(existingConversation.get(), participantUser);
        }

        Conversation conversation = Conversation.createPrivate(authenticatedUser);

        conversationRepository.save(conversation);

        var member1 = ConversationMember.createPrivate(conversation, authenticatedUser);
        var member2 = ConversationMember.createPrivate(conversation, participantUser);

        conversationMemberRepository.saveAll(List.of(member1, member2));

        return PrivateConversationResponse.toResponse(conversation, participantUser);
    }
}

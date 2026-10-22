package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
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
    public PrivateConversationResponse createPrivate(User creatorUser, CreatePrivateConversationRequest request) {
        UUID creatorId = creatorUser.getId();
        UUID participantId = request.participantId();

        validateDistinctUsers(creatorId, participantId);

        User participantUser = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found."));

        return conversationMemberRepository.findPrivateConversationBetweenUsers(creatorId, participantId)
                .map(existing -> PrivateConversationResponse.toResponse(existing, participantUser))
                .orElseGet(() -> {
                    Conversation newConversation = createAndPersistPrivateConversation(creatorUser, participantUser);
                    return PrivateConversationResponse.toResponse(newConversation, participantUser);
                });

    }

    private void validateDistinctUsers(UUID userIdA, UUID userIdB) {
        if (userIdA.equals(userIdB)){
            throw new RuntimeException("Not allowed start private conversation with yourself.");
        }
    }

    private Conversation createAndPersistPrivateConversation(User creator, User participant) {
        Conversation conversation = Conversation.createPrivate(creator);

        conversationRepository.save(conversation);

        var member1 = ConversationMember.createPrivate(conversation, creator);
        var member2 = ConversationMember.createPrivate(conversation, participant);

        conversationMemberRepository.saveAll(List.of(member1, member2));

        return conversation;
    }
}

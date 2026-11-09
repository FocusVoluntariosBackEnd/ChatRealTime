package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.exception.SameUsersConversationException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CreatePrivateConversationUseCase {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public PrivateConversationResponse execute(UUID creatorId, CreatePrivateConversationRequest request) {
        UUID participantId = request.participantId();

        validateDistinctUsers(creatorId, participantId);

        User creatorUser = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        User participantUser = userRepository.findById(request.participantId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant user not found."));

        OrderedUsers ordered = determineUserOrder(creatorUser, participantUser);

        Optional<Conversation> existingConversation = conversationRepository.findPrivateBetween(
                ordered.userA().getId(),
                ordered.userB().getId()
        );

        if (existingConversation.isPresent()) {
            return PrivateConversationResponse.toResponse(existingConversation.get(), participantUser);
        }

        try {
            Conversation created = savePrivateConversation(creatorUser, participantUser, ordered.userA, ordered.userB);

            return PrivateConversationResponse.toResponse(created, participantUser);
        } catch (DataIntegrityViolationException ex) {
            return findExistingConversationOrThrow(ordered.userA().getId(), ordered.userB().getId(), participantUser, ex);
        }
    }

    private void validateDistinctUsers(UUID userIdA, UUID userIdB) {
        if (userIdA.equals(userIdB)){
            throw new SameUsersConversationException("Cannot start private conversation with yourself.");
        }
    }

    private OrderedUsers determineUserOrder(User creator, User participant) {
        String creatorIdStr = creator.getId().toString();
        String participantIdStr = participant.getId().toString();

        if (creatorIdStr.compareTo(participantIdStr) < 0) {
            return new OrderedUsers(creator, participant);
        } else {
            return new OrderedUsers(participant, creator);
        }
    }

    @Transactional
    protected Conversation savePrivateConversation(User creator, User participant, User userA, User userB) {
        Conversation conversation = Conversation.createPrivate(creator, userA, userB);
        conversationRepository.saveAndFlush(conversation);

        var member1 = ConversationMember.createMember(conversation, creator);
        var member2 = ConversationMember.createMember(conversation, participant);
        conversationMemberRepository.saveAll(List.of(member1, member2));

        return conversation;
    }

    private PrivateConversationResponse findExistingConversationOrThrow(UUID userAId, UUID userBId, User participant, DataIntegrityViolationException ex) {
        return conversationRepository.findPrivateBetween(userAId, userBId)
                .map(c -> PrivateConversationResponse.toResponse(c, participant))
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve concurrent conversation.", ex));
    }

    private record OrderedUsers(User userA, User userB) {}
}

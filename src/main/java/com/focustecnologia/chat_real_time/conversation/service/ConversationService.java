package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.common.redis.DistributedLockService;
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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final DistributedLockService lockService;

    @Transactional
    public PrivateConversationResponse createPrivate(User creatorUser, CreatePrivateConversationRequest request) {
        UUID creatorId = creatorUser.getId();
        UUID participantId = request.participantId();

        validateDistinctUsers(creatorId, participantId);

        User participantUser = userRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found."));

        String lockKey = buildLockKey(creatorId, participantId);
        boolean acquired = lockService.acquireLock(lockKey, Duration.ofSeconds(5));

        if (acquired) {
            try {
                return conversationMemberRepository.findPrivateConversationBetweenUsers(creatorId, participantId)
                        .map(existing -> PrivateConversationResponse.toResponse(existing, participantUser))
                        .orElseGet(() -> {
                            Conversation newConversation = createAndPersistPrivateConversation(creatorUser, participantUser);
                            return PrivateConversationResponse.toResponse(newConversation, participantUser);
                        });
            } finally {
                lockService.releaseLock(lockKey);
            }
        } else {
            waitForAWhile(300);
            return conversationMemberRepository.findPrivateConversationBetweenUsers(creatorId, participantId)
                    .map(existing -> PrivateConversationResponse.toResponse(existing, participantUser))
                    .orElseThrow(() -> new IllegalStateException("Failed retrieve concurrent conversation."));
        }
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

    private String buildLockKey(UUID userIdA, UUID userIdB) {
        if (userIdA.compareTo(userIdB) < 0){
            return "lock:conversation:" + userIdA + ":" + userIdB;
        } else {
            return "lock:conversation:" + userIdB + ":" + userIdA;
        }
    }

    private void waitForAWhile(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException("Thread interrupted while waiting for lock", ex);
        }
    }
}

package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserIdAndRole(UUID conversationId, UUID userId, Role role);
}

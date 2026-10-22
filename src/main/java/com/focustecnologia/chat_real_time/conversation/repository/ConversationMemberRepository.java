package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    @Query("""
            SELECT cm.conversation
            FROM ConversationMember cm
            WHERE cm.conversation.type = 'PRIVATE'
                AND cm.user.id IN (:userAId, :userBId)
            GROUP BY cm.conversation
            HAVING COUNT (cm.id) = 2
            """)
    Optional<Conversation> findPrivateConversationBetweenUsers(@Param("userAId") UUID userAId,
                                                               @Param("userBId") UUID userBId);
}

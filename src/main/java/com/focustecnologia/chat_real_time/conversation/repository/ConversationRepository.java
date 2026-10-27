package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("""
            SELECT c
            FROM Conversation c
            WHERE c.type = 'PRIVATE'
                        AND c.userA.id = :userAId
                        AND c.userB.id = :userBId
            """)
    Optional<Conversation> findPrivateBetween(@Param("userAId") UUID userAId,
                                              @Param("userBId") UUID userBId);
}

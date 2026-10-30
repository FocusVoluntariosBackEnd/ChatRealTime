package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.dto.ConversationSumaryResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
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

    Optional<Conversation> findByIdAndType(UUID id, Type type);

    @Query(
            value = """
        SELECT new com.focustecnologia.chat_real_time.conversation.dto.ConversationSumaryResponse(
            c.id,
            c.type,
            CASE
                WHEN a.id = :userId THEN b.name
                WHEN b.id = :userId THEN a.name
                ELSE c.name
            END,
            c.createdAt
        )
        FROM ConversationMember cm
        JOIN cm.conversation c
        LEFT JOIN c.userA a
        LEFT JOIN c.userB b
        WHERE cm.user.id = :userId
        ORDER BY c.createdAt DESC, c.id DESC
        """,
            countQuery = """
        SELECT COUNT(cm)
        FROM ConversationMember cm
        WHERE cm.user.id = :userId
        """
    )
    Page<ConversationSumaryResponse> findAllForUser(
            @Param("userId") UUID userId,
            Pageable pageable
    );

}

package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {
    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserIdAndRole(UUID conversationId, UUID userId, Role role);

    Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<ConversationMember> findAllByConversationId(UUID conversationId);

    @Query("""
            select distinct member.user.id
            from ConversationMember member
            where member.user.id <> :userId
              and member.conversation.id in (
                  select ownMembership.conversation.id
                  from ConversationMember ownMembership
                  where ownMembership.user.id = :userId
              )
            """)
    List<UUID> findDistinctUserIdsSharingConversationWith(@Param("userId") UUID userId);

    @Query("""
            select count(member)
            from ConversationMember member
            where member.user.id = :recipientId
              and member.conversation.id in (
                  select ownMembership.conversation.id
                  from ConversationMember ownMembership
                  where ownMembership.user.id = :subjectUserId
              )
            """)
    long countSharedConversations(
            @Param("recipientId") UUID recipientId,
            @Param("subjectUserId") UUID subjectUserId
    );
}

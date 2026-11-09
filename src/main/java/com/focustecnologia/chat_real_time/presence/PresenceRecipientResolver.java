package com.focustecnologia.chat_real_time.presence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceRecipientResolver {
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<UUID> findRecipients(UUID subjectUserId) {
        return entityManager.createQuery("""
                select distinct member.user.id
                from ConversationMember member
                where member.user.id <> :subjectUserId
                  and member.conversation.id in (
                      select ownMembership.conversation.id
                      from ConversationMember ownMembership
                      where ownMembership.user.id = :subjectUserId
                  )
                """, UUID.class)
                .setParameter("subjectUserId", subjectUserId)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public boolean canReceive(UUID recipientId, UUID subjectUserId) {
        Long sharedConversations = entityManager.createQuery("""
                select count(member)
                from ConversationMember member
                where member.user.id = :recipientId
                  and member.conversation.id in (
                      select ownMembership.conversation.id
                      from ConversationMember ownMembership
                      where ownMembership.user.id = :subjectUserId
                  )
                """, Long.class)
                .setParameter("recipientId", recipientId)
                .setParameter("subjectUserId", subjectUserId)
                .getSingleResult();

        return sharedConversations > 0;
    }
}

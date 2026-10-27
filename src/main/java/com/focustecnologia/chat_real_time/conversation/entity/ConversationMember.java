package com.focustecnologia.chat_real_time.conversation.entity;

import com.focustecnologia.chat_real_time.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
@Getter
public class ConversationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    protected ConversationMember() {}

    public ConversationMember(Conversation conversation, User user, Role role) {
        this.conversation = conversation;
        this.user = user;
        this.role = role;
    }

    public static ConversationMember createPrivate(Conversation conversation, User member) {
        return new ConversationMember(conversation, member, Role.MEMBER);
    }
}

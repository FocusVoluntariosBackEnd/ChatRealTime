package com.focustecnologia.chat_real_time.conversation.entity;

import com.focustecnologia.chat_real_time.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Conversation() {}

    public Conversation(Type type, String name, User createdBy) {
        this.type = type;
        this.name = name;
        this.createdBy = createdBy;
    }

    public static Conversation createPrivate(User creator) {
        return new  Conversation(Type.PRIVATE, null, creator);
    }
}

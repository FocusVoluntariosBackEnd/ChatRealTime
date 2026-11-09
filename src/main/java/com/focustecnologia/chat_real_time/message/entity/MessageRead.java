package com.focustecnologia.chat_real_time.message.entity;

import com.focustecnologia.chat_real_time.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages_read")
@Getter
public class MessageRead {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "read_at", nullable = false, updatable = false)
    private Instant readAt = Instant.now();

    protected MessageRead() {}

    public MessageRead(Message message, User user) {
        this.message = message;
        this.user = user;
    }
}

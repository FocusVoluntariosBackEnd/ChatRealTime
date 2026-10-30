package com.focustecnologia.chat_real_time.message.repository;

import com.focustecnologia.chat_real_time.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @EntityGraph(attributePaths = "sender")
    Slice<Message> findByConversationId(UUID conversationId, Pageable pageable);
}

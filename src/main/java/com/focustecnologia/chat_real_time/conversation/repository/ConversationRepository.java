package com.focustecnologia.chat_real_time.conversation.repository;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> { }

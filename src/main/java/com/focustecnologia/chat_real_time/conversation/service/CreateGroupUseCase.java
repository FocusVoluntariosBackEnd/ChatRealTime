package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreateGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.GroupResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CreateGroupUseCase {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse execute(UUID creatorId, CreateGroupRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Conversation conversation = Conversation.createGroup(creator, request.name());
        conversationRepository.save(conversation);

        ConversationMember admin = ConversationMember.createAdmin(conversation, creator);
        conversationMemberRepository.save(admin);

        List<ConversationMember> members = new ArrayList<>();

        members.add(admin);

        Set<UUID> memberIds = new HashSet<>(request.membersIds());

        for (UUID participantId : memberIds) {
            creator.validateDistinctUser(participantId);

            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found."));

            ConversationMember member = ConversationMember.createMember(conversation, participant);
            conversationMemberRepository.save(member);

            members.add(member);
        }

        return GroupResponse.toResponse(conversation, members);
    }
}

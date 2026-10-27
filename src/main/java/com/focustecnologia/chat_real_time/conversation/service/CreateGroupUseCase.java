package com.focustecnologia.chat_real_time.conversation.service;

import com.focustecnologia.chat_real_time.conversation.dto.CreateGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.GroupResponse;
import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.exception.ResourceNotFoundException;
import com.focustecnologia.chat_real_time.exception.SameUsersConversationException;
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
        Set<UUID> memberIds = new HashSet<>(request.membersIds());

        validateDistinctUsers(creatorId, memberIds);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        List<User> participants = getParticipants(memberIds);

        Conversation conversation = createConversation(creator, request.name());
        List<ConversationMember> members = createMembers(conversation, creator, participants);

        return GroupResponse.toResponse(conversation, members);
    }

    private void validateDistinctUsers(UUID creatorId, Set<UUID> memberIds) {
        if (memberIds.contains(creatorId)) {
            throw new SameUsersConversationException("Cannot start group conversation with yourself.");
        }
    }


    private List<User> getParticipants(Set<UUID> memberIds) {
        List<User> users = userRepository.findAllById(memberIds);

        if (users.size() != memberIds.size()) {
            throw new ResourceNotFoundException("One or more users not found.");
        }

        return users;
    }

    private Conversation createConversation(User creator, String name) {
        Conversation conversation = Conversation.createGroup(creator, name);
        return conversationRepository.save(conversation);
    }

    private List<ConversationMember> createMembers(Conversation conversation, User creator, List<User> participants) {
        List<ConversationMember> members = new ArrayList<>();

        members.add(ConversationMember.createAdmin(conversation, creator));

        participants.stream()
                .map(participant -> ConversationMember.createMember(conversation, participant))
                .forEach(members::add);

        return conversationMemberRepository.saveAll(members);
    }
}

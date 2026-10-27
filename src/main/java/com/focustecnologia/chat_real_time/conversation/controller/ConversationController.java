package com.focustecnologia.chat_real_time.conversation.controller;

import com.focustecnologia.chat_real_time.conversation.dto.CreateGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.GroupResponse;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.service.CreateGroupUseCase;
import com.focustecnologia.chat_real_time.conversation.service.CreatePrivateConversationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final CreatePrivateConversationUseCase createPrivateUseCase;
    private final CreateGroupUseCase createGroupUseCase;

    @PostMapping("/private")
    public ResponseEntity<PrivateConversationResponse> createPrivate(@AuthenticationPrincipal Jwt jwt,
                                                                     @Valid @RequestBody CreatePrivateConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        PrivateConversationResponse response = createPrivateUseCase.execute(userId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/group")
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody CreateGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        GroupResponse response = createGroupUseCase.execute(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

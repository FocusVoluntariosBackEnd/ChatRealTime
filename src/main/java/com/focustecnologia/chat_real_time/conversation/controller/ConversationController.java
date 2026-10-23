package com.focustecnologia.chat_real_time.conversation.controller;

import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.service.ConversationService;
import com.focustecnologia.chat_real_time.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService service;

    @PostMapping("/private")
    public ResponseEntity<PrivateConversationResponse> createPrivate(@AuthenticationPrincipal User authenticatedUser,
                                                                     @Valid @RequestBody CreatePrivateConversationRequest request) {
        PrivateConversationResponse response = service.createPrivate(authenticatedUser, request);

        return ResponseEntity.ok(response);
    }
}

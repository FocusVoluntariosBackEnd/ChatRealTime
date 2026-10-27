package com.focustecnologia.chat_real_time.conversation.controller;

import com.focustecnologia.chat_real_time.conversation.dto.ConversationSumaryResponse;
import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.service.CreatePrivateConversationUseCase;
import com.focustecnologia.chat_real_time.conversation.service.FindAllConversationsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final CreatePrivateConversationUseCase createPrivateUseCase;
    private final FindAllConversationsUseCase findAllConversationsUseCase;

    @PostMapping("/private")
    public ResponseEntity<PrivateConversationResponse> createPrivate(@AuthenticationPrincipal Jwt jwt,
                                                                     @Valid @RequestBody CreatePrivateConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        PrivateConversationResponse response = createPrivateUseCase.execute(userId, request);

        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<Page<ConversationSumaryResponse>> findAll (
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int size) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(findAllConversationsUseCase.execute(userId, page, size));

    }

}

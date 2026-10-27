package com.focustecnologia.chat_real_time.conversation.controller;

import com.focustecnologia.chat_real_time.conversation.dto.CreateGroupRequest;
import com.focustecnologia.chat_real_time.conversation.dto.CreatePrivateConversationRequest;
import com.focustecnologia.chat_real_time.conversation.dto.GroupResponse;
import com.focustecnologia.chat_real_time.conversation.dto.PrivateConversationResponse;
import com.focustecnologia.chat_real_time.conversation.service.CreateGroupUseCase;
import com.focustecnologia.chat_real_time.conversation.service.CreatePrivateConversationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Conversas", description = "Endpoints para gerenciamento e criação de conversas privadas e em grupo")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final CreatePrivateConversationUseCase createPrivateUseCase;
    private final CreateGroupUseCase createGroupUseCase;

    @Operation(summary = "Criar ou obter conversa privada", description = "Cria uma nova conversa direta entre o usuário autenticado e o destinatário, ou retorna a conversa existente se já houver uma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversa privada criada ou recuperada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário destinatário não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: o usuário informado é o próprio usuário autenticado", content = @Content)
    })
    @PostMapping("/private")
    public ResponseEntity<PrivateConversationResponse> createPrivate(@AuthenticationPrincipal Jwt jwt,
                                                                     @Valid @RequestBody CreatePrivateConversationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        PrivateConversationResponse response = createPrivateUseCase.execute(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar grupo de conversa", description = "Cria um novo grupo de conversa com os participantes informados, definindo o usuário autenticado como administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grupo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Um ou mais participantes não encontrados", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: um dos membros informados é o próprio usuário autenticado", content = @Content)
    })
    @PostMapping("/group")
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody CreateGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        GroupResponse response = createGroupUseCase.execute(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

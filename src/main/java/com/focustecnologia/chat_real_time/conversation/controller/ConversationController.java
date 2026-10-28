package com.focustecnologia.chat_real_time.conversation.controller;

import com.focustecnologia.chat_real_time.conversation.dto.*;
import com.focustecnologia.chat_real_time.conversation.service.AddMemberGroupUseCase;
import com.focustecnologia.chat_real_time.conversation.service.CreateGroupUseCase;
import com.focustecnologia.chat_real_time.conversation.service.CreatePrivateConversationUseCase;
import com.focustecnologia.chat_real_time.conversation.service.DeleteMemberGroupUseCase;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Conversas", description = "Endpoints para gerenciamento de conversas privadas e grupos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final CreatePrivateConversationUseCase createPrivateUseCase;
    private final CreateGroupUseCase createGroupUseCase;
    private final AddMemberGroupUseCase addMemberGroupUseCase;
    private final DeleteMemberGroupUseCase deleteMemberGroupUseCase;

    @Operation(summary = "Criar ou obter conversa privada", description = "Cria uma nova conversa direta entre o usuário autenticado e o destinatário, ou retorna a conversa existente se já houver uma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversa privada obtida ou criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Destinatário não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: tentativa de criar conversa consigo mesmo", content = @Content)
    })
    @PostMapping("/private")
    public ResponseEntity<PrivateConversationResponse> createOrGetPrivate(@AuthenticationPrincipal Jwt jwt,
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
    @PostMapping("/groups")
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody CreateGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        GroupResponse response = createGroupUseCase.execute(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Adicionar membro ao grupo", description = "Adiciona um novo membro a um grupo de conversa existente. Apenas administradores do grupo têm permissão para realizar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem adicionar membros ao grupo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo ou usuário a ser adicionado não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: o usuário já é membro do grupo ou tentativa de adicionar a si mesmo", content = @Content)
    })
    @PostMapping("/groups/{id}/members")
    public ResponseEntity<ConversationMemberResponse> addMemberGroup(@PathVariable("id") UUID groupId,
                                                                     @AuthenticationPrincipal Jwt jwt,
                                                                     @Valid @RequestBody AddMemberGroupRequest request
                                                                     ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        ConversationMemberResponse response = addMemberGroupUseCase.execute(userId, groupId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Remover membro do grupo",
            description = "Remove um membro de um grupo de conversa existente. Apenas administradores do grupo têm permissão para realizar esta operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem remover membros do grupo", content = @Content),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito: tentativa de remover a si próprio", content = @Content)
    })
    @DeleteMapping("/groups/{id}/members/{userId}")
    public ResponseEntity<Void> deleteMemberGroup(@PathVariable("id") UUID groupId,
                                                  @PathVariable("userId") UUID memberId,
                                                  @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        deleteMemberGroupUseCase.execute(userId, groupId, memberId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

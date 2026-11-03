package com.focustecnologia.chat_real_time.message.controller;

import com.focustecnologia.chat_real_time.message.service.MarkMessageAsReadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Mensagens", description = "Endpoints para gerenciamento de mensagens")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MarkMessageAsReadUseCase markMessageAsReadUseCase;

    @Operation(
            summary = "Marcar mensagem como lida",
            description = "Registra a leitura da mensagem pelo usuário autenticado. "
                    + "Uma leitura já registrada permanece sem alterações."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Leitura registrada ou já existente"),
            @ApiResponse(responseCode = "400", description = "ID da mensagem inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada ou inacessível", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade em solicitações simultâneas", content = @Content)
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") UUID messageId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        markMessageAsReadUseCase.execute(userId, messageId);

        return ResponseEntity.noContent().build();
    }
}

package com.focustecnologia.chat_real_time.notification.controller;

import com.focustecnologia.chat_real_time.notification.dto.NotificationListResponse;
import com.focustecnologia.chat_real_time.notification.service.FindNotificationsUseCase;
import com.focustecnologia.chat_real_time.notification.service.MarkNotificationAsReadUseCase;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@Tag(name = "Notificações", description = "Consulta das notificações do usuário autenticado")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final FindNotificationsUseCase findNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Operation(
            summary = "Listar notificações",
            description = "Retorna as notificações persistidas do usuário autenticado, "
                    + "da mais recente para a mais antiga, com paginação por page e limit."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificações retornadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<NotificationListResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                findNotificationsUseCase.execute(userId, page, limit)
        );
    }

    @Operation(
            summary = "Marcar notificação como lida",
            description = "Marca como lida uma notificação pertencente ao usuário autenticado. "
                    + "Uma notificação já lida permanece sem alterações."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida ou já lida"),
            @ApiResponse(responseCode = "400", description = "ID da notificação inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada ou inacessível", content = @Content)
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") UUID notificationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        markNotificationAsReadUseCase.execute(userId, notificationId);

        return ResponseEntity.noContent().build();
    }
}

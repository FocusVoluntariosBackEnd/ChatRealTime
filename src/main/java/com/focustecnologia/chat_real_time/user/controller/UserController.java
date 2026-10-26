package com.focustecnologia.chat_real_time.user.controller;

import com.focustecnologia.chat_real_time.auth.controller.response.UserResponse;
import com.focustecnologia.chat_real_time.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Usuários", description = "Busca e paginação de usuários")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @Operation(summary = "Buscar usuário pelo Id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @Operation(summary = "Paginação de usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuários"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAll(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "25") int size) {
        return ResponseEntity.ok(service.getAll(page, size));
    }
}

package com.focustecnologia.chat_real_time.auth.controller;

import com.focustecnologia.chat_real_time.auth.controller.request.LoginRequest;
import com.focustecnologia.chat_real_time.auth.controller.request.UserRequest;
import com.focustecnologia.chat_real_time.auth.controller.response.LoginResponse;
import com.focustecnologia.chat_real_time.auth.controller.response.UserResponse;
import com.focustecnologia.chat_real_time.auth.service.LoginService;
import com.focustecnologia.chat_real_time.auth.service.RegisterService;
import com.focustecnologia.chat_real_time.auth.service.output.LoginOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints públicos para cadastro e autenticação de usuários")
@SecurityRequirements
@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final LoginService loginService;
    private final RegisterService  registerService;

    public AuthController(LoginService loginService, RegisterService registerService) {
        this.loginService = loginService;
        this.registerService = registerService;
    }

    @Operation(summary = "Cadastrar usuário", description = "Cria um usuário com nome, e-mail e senha e retorna seus dados sem a senha.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content)
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse saveUser(@Valid @RequestBody UserRequest request) {
        var user = registerService.execute(request.toInput());
        return UserResponse.from(user);
    }

    @Operation(summary = "Autenticar usuário", description = "Valida e-mail e senha e retorna um token de acesso JWT, seu tipo e tempo de validade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos", content = @Content)
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws Exception {
        LoginOutput login = loginService.execute(request.toLoginInput());
        return LoginResponse.from(login);
    }

}

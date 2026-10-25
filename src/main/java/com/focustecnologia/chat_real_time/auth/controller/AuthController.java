package com.focustecnologia.chat_real_time.auth.controller;

import com.focustecnologia.chat_real_time.auth.controller.request.LoginRequest;
import com.focustecnologia.chat_real_time.auth.controller.request.UserRequest;
import com.focustecnologia.chat_real_time.auth.controller.response.LoginResponse;
import com.focustecnologia.chat_real_time.auth.controller.response.UserResponse;
import com.focustecnologia.chat_real_time.auth.service.LoginService;
import com.focustecnologia.chat_real_time.auth.service.RegisterService;
import com.focustecnologia.chat_real_time.auth.service.output.LoginOutput;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final LoginService loginService;
    private final RegisterService  registerService;

    public AuthController(LoginService loginService, RegisterService registerService) {
        this.loginService = loginService;
        this.registerService = registerService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse saveUser(@Valid @RequestBody UserRequest request) {
        var user = registerService.execute(request.toInput());
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws Exception {
        LoginOutput login = loginService.execute(request.toLoginInput());
        return LoginResponse.from(login);
    }

}

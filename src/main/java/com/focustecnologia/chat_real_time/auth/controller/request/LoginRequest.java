package com.focustecnologia.chat_real_time.auth.controller.request;

import com.focustecnologia.chat_real_time.auth.service.input.LoginInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatorio!")
        @Email(message = "E-mail inválido!")
        String email,

        @NotBlank(message = "A senha é obrigatoria!")
        String password) {

    public LoginInput toLoginInput() {
        return new LoginInput(email, password);
    }

}

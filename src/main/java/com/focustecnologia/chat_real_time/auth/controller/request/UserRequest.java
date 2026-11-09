package com.focustecnologia.chat_real_time.auth.controller.request;

import com.focustecnologia.chat_real_time.auth.service.input.UserInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest (

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30, message = "O nome deve ter no máximo 30 caracteres")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 60, message = "E-mail deve ter no máximo 60 caracteres")
        String email,

        @NotBlank(message = "Senha obrigatória")
        @Size(min = 8, max = 15, message = "Senha deve ter entre 8 e 15 caracteres")
        String password) {

    public UserInput toInput() {
        return new UserInput(name, email, password);
    }
}

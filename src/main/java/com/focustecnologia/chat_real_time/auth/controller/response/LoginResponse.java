package com.focustecnologia.chat_real_time.auth.controller.response;

import com.focustecnologia.chat_real_time.auth.service.output.LoginOutput;

public record LoginResponse(String accessToken, String tokenType, Long expiresIn) {
    public static LoginResponse from(LoginOutput loginOutput) {
        return new LoginResponse(
                loginOutput.accessToken(),
                loginOutput.tokenType(),
                loginOutput.expiresIn()
        );
    }
}

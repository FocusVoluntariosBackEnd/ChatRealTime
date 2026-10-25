package com.focustecnologia.chat_real_time.auth.service.output;

public record LoginOutput(String accessToken, String tokenType, Long expiresIn) {
}

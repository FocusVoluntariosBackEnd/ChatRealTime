package com.focustecnologia.chat_real_time.auth.controller.response;

import com.focustecnologia.chat_real_time.auth.service.output.UserOutput;

import java.time.Instant;

public record UserResponse(
        String id,
        String name,
        String email,
        Instant createdAt) {

    public static UserResponse from(UserOutput userOutput) {
        return new UserResponse(
                userOutput.id(),
                userOutput.name(),
                userOutput.email(),
                userOutput.createdAt()
        );
    }

}

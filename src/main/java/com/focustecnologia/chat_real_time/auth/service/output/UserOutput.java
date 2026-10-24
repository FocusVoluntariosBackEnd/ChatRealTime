package com.focustecnologia.chat_real_time.auth.service.output;

import com.focustecnologia.chat_real_time.user.entity.User;

import java.time.Instant;

public record UserOutput (
        String id,
        String name,
        String email,
        Instant createdAt) {

    public static UserOutput fromUser(User user) {
        return new UserOutput(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}

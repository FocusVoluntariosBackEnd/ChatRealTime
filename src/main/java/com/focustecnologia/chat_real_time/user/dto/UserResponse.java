package com.focustecnologia.chat_real_time.user.dto;

import com.focustecnologia.chat_real_time.user.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email
) {
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}

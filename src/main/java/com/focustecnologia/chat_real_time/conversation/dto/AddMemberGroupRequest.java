package com.focustecnologia.chat_real_time.conversation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberGroupRequest(
        @NotNull
        UUID memberId
) {
}

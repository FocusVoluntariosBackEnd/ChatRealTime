package com.focustecnologia.chat_real_time.user.dto;

import java.util.UUID;

public record ParticipantResponse (
        UUID id,
        String name,
        String email
) {}

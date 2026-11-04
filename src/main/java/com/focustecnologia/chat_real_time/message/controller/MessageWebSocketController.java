package com.focustecnologia.chat_real_time.message.controller;

import com.focustecnologia.chat_real_time.message.dto.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.MessageSendPayload;
import com.focustecnologia.chat_real_time.message.service.SendMessageUseCase;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {
    private final SendMessageUseCase sendMessageUseCase;

    @MessageMapping("/messages.send")
    @SendToUser("/queue/messages.ack")
    public WebSocketEvent<MessageAckPayload> sendMessage(@Valid @Payload WebSocketEvent<MessageSendPayload> payload,
                                                         Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());

        log.info("Received 'message:send' event from user {} for conversation {}", senderId, payload.data().conversationId());

        MessageAckPayload ack = sendMessageUseCase.execute(senderId, payload.data());

        return WebSocketEvent.toAck(ack);
    }
}

package com.focustecnologia.chat_real_time.message.controller;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageAckPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageReadPayload;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageSendPayload;
import com.focustecnologia.chat_real_time.message.service.MarkMessageAsReadUseCase;
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
    private final MarkMessageAsReadUseCase markMessageAsReadUseCase;

    @MessageMapping("/messages.send")
    @SendToUser("/queue/messages.ack")
    public WebSocketEvent<MessageAckPayload> ackMessage(@Valid @Payload WebSocketEvent<MessageSendPayload> payload,
                                                         Principal principal) {
        UUID senderId = UUID.fromString(principal.getName());

        log.info("Received 'message:send' event from user {} for conversation {}", senderId, payload.data().conversationId());

        MessageAckPayload body = sendMessageUseCase.execute(senderId, payload.data());

        return WebSocketEvent.toMessageAck(body);
    }

    @MessageMapping("/messages.read")
    public void readMessage(@Valid @Payload WebSocketEvent<MessageReadPayload> payload,
                            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());

        log.info("Received 'message:read' event from user {} for conversation {} and message {}", userId, payload.data().conversationId(), payload.data().messageId());

        markMessageAsReadUseCase.execute(userId, payload.data().messageId());
    }
}

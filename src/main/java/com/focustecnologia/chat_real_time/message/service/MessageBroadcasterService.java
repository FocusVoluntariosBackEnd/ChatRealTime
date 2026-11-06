package com.focustecnologia.chat_real_time.message.service;

import com.focustecnologia.chat_real_time.message.dto.websocket.MessageNewPayload;
import com.focustecnologia.chat_real_time.message.entity.Message;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageBroadcasterService {
    private final SimpMessagingTemplate messagingTemplate;

    public void transmitMessage(Message message) {
        MessageNewPayload payload = MessageNewPayload.toBody(message);

        WebSocketEvent<MessageNewPayload> body = WebSocketEvent.toNew(payload);

        messagingTemplate.convertAndSend("/topic/conversations/" + payload.conversationId(), body);
    }
}

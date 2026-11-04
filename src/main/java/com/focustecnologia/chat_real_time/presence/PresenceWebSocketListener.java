package com.focustecnologia.chat_real_time.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class PresenceWebSocketListener {
    private final PresenceService presenceService;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = headers.getUser();
        String sessionId = headers.getSessionId();

        if (user != null && sessionId != null) {
            presenceService.connected(user.getName(), sessionId);
        }
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        presenceService.disconnected(event.getSessionId());
    }
}

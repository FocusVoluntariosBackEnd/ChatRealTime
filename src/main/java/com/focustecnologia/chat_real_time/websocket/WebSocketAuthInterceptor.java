package com.focustecnologia.chat_real_time.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtDecoder decoder;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        var stompFrame = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (stompFrame != null && StompCommand.CONNECT.equals(stompFrame.getCommand())) {
            String authHeader = stompFrame.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket connection attempt rejected: Authorization header missing or malformed");
                throw new BadCredentialsException("WebSocket connection rejected");
            }

            String token = authHeader.substring(7);

            try {
                Jwt jwt = decoder.decode(token);
                var auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
                stompFrame.setUser(auth);
                log.info("User authenticated successfully in WebSocket: {}", jwt.getSubject());
            } catch (JwtException ex) {
                throw new BadCredentialsException("Jwt token invalid or expired");
            }
        }

        return message;
    }
}

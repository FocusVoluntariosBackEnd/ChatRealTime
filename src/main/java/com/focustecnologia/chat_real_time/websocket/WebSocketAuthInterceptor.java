package com.focustecnologia.chat_real_time.websocket;

import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.exception.ForbiddenException;
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

import java.security.Principal;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtDecoder decoder;
    private final ConversationMemberRepository memberRepository;

    private static final String CONVERSATION_TOPIC_PREFIX = "/topic/conversations/";

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        }
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                handleSubscribe(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WebSocket connection rejected: Authorization header missing or malformed.");
            throw new BadCredentialsException("WebSocket connection rejected.");
        }

        String token = authHeader.substring(7);

        try {
            Jwt jwt = decoder.decode(token);
            var auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
            accessor.setUser(auth);
            log.info("User authenticated successfully in WebSocket CONNECT: {}", jwt.getSubject());
        } catch (JwtException ex) {
            log.error("JWT decoding failed during WebSocket connect: {}", ex.getMessage());
            throw new BadCredentialsException("Jwt token invalid or expired.");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith(CONVERSATION_TOPIC_PREFIX)) {
            UUID conversationId = UUID.fromString(destination.substring(CONVERSATION_TOPIC_PREFIX.length()));

            UUID authenticatedUserId = UUID.fromString(accessor.getUser().getName());

            boolean isMember = memberRepository.existsByConversationIdAndUserId(conversationId, authenticatedUserId);

            if (!isMember) {
                log.warn("User {} tried to subscribe to unauthorized conversation topic {}", authenticatedUserId, conversationId);
                throw new ForbiddenException("Cannot subscribe to conversation.");
            }

            log.info("User {} authorized to subscribe to conversation topic {}", authenticatedUserId, conversationId);
        }
    }
}

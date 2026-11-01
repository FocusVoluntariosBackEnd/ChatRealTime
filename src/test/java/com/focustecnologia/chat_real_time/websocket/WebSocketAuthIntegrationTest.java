package com.focustecnologia.chat_real_time.websocket;

import com.focustecnologia.chat_real_time.security.JwtService;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketAuthIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    @Test
    void shouldConnectSuccessfullyWhenTokenIsValid() throws Exception {
        String randomEmail = "test.ws." + UUID.randomUUID() + "@focustech.com";
        User user = userRepository.save(new User("WebSocket Test", randomEmail, "password"));

        String token = jwtService.generateToken(user);

        String url = "ws://localhost:" + port + "/ws";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<StompSession> completableFuture = new CompletableFuture<>();

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

        stompClient.connectAsync(url, handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                completableFuture.complete(session);
            }
        });

        StompSession session = completableFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(session);
        assertTrue(session.isConnected());

        session.disconnect();
    }

    @Test
    void shouldFailToConnectWhenTokenIsInvalid() throws Exception {
        String url =  "ws://localhost:" + port + "/ws";
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer invalid_token");

        CompletableFuture<StompSession> completableFuture = new CompletableFuture<>();

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

        stompClient.connectAsync(url, handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                completableFuture.complete(session);
            }
        });

        assertThrows(Exception.class, () -> completableFuture.get(5, TimeUnit.SECONDS));
    }
}

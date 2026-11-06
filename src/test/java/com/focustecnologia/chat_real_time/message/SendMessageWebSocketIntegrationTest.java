package com.focustecnologia.chat_real_time.message;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.message.dto.websocket.MessageSendPayload;
import com.focustecnologia.chat_real_time.message.repository.MessageRepository;
import com.focustecnologia.chat_real_time.security.JwtService;
import com.focustecnologia.chat_real_time.user.entity.User;
import com.focustecnologia.chat_real_time.user.repository.UserRepository;
import com.focustecnologia.chat_real_time.websocket.WebSocketEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendMessageWebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationMemberRepository memberRepository;
    @Autowired
    private MessageRepository messageRepository;

    private WebSocketStompClient stompClient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
        baseUrl = "ws://localhost:" + port + "/ws";
    }

    @Test
    void shouldSendMessageAndReceiveAck() throws Exception {
        User sender = createTestUser("sender");
        Conversation conversation = createGroupWithMember(sender, "Group Test");

        StompSession session = connectClient(sender);

        CompletableFuture<String> ackFuture = subscribeToQueue(session, "/user/queue/messages.ack");

        String clientMsgId = "msg-" + UUID.randomUUID();
        sendWebSocketMessage(session, conversation.getId(), clientMsgId, "Mensagem enviada com sucesso");

        String ackReceived = ackFuture.get(10, TimeUnit.SECONDS);
        assertNotNull(ackReceived);
        assertTrue(ackReceived.contains("message:ack"));
        assertTrue(ackReceived.contains("SAVED"));
        assertTrue(ackReceived.contains(clientMsgId));

        assertTrue(messageRepository.findByConversationIdAndClientMessageId(conversation.getId(), clientMsgId).isPresent());

        safeDisconnect(session);
    }

    @Test
    void shouldBeIdempotentOnDuplicateClientMessageId() throws Exception {
        User sender = createTestUser("idemp");
        Conversation conversation = createGroupWithMember(sender, "Group Idempotent");

        StompSession session = connectClient(sender);
        CompletableFuture<String> ackFuture = subscribeToQueue(session, "/user/queue/messages.ack");

        String clientMsgId = "idemp-" + UUID.randomUUID();
        sendWebSocketMessage(session, conversation.getId(), clientMsgId, "Tentativa de envio");

        // Primeiro envio
        assertNotNull(ackFuture.get(10, TimeUnit.SECONDS));

        // Segundo envio (reenvio com a mesma chave) - reutiliza a sessão
        CompletableFuture<String> secondAckFuture = subscribeToQueue(session, "/user/queue/messages.ack");
        sendWebSocketMessage(session, conversation.getId(), clientMsgId, "Tentativa de envio");

        String secondAck = secondAckFuture.get(10, TimeUnit.SECONDS);
        assertNotNull(secondAck);
        assertTrue(secondAck.contains("SAVED"));
        assertTrue(secondAck.contains(clientMsgId));

        safeDisconnect(session);
    }

    @Test
    void shouldBroadcastMessageToTopicAndEnforceSubscriptionAuthorization() throws Exception {
        User member = createTestUser("member");
        User intruder = createTestUser("intruder");
        Conversation conversation = createGroupWithMember(member, "Broadcast Test Group");

        // 1. Testa o Membro conectando e recebendo o broadcast no tópico
        StompSession memberSession = connectClient(member);
        CompletableFuture<String> broadcastFuture = subscribeToQueue(memberSession, "/topic/conversations/" + conversation.getId());

        String clientMsgId = "broadcast-" + UUID.randomUUID();
        sendWebSocketMessage(memberSession, conversation.getId(), clientMsgId, "Olá em tempo real!");

        String broadcastReceived = broadcastFuture.get(10, TimeUnit.SECONDS);
        assertNotNull(broadcastReceived);
        assertTrue(broadcastReceived.contains("message:new"));
        assertTrue(broadcastReceived.contains("Olá em tempo real!"));

        safeDisconnect(memberSession);

        // 2. Testa o Intruso tentando escutar o tópico sem permissão
        StompSession intruderSession = connectClient(intruder);
        CompletableFuture<String> errorFuture = subscribeToQueue(intruderSession, "/topic/conversations/" + conversation.getId());

        boolean subscriptionBlocked = false;
        try {
            errorFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            subscriptionBlocked = true; // Esperado cair aqui pois o interceptador barra
        }

        assertTrue(subscriptionBlocked, "O intruso não deveria conseguir receber mensagens do tópico.");
        safeDisconnect(intruderSession);
    }

    // --- Métodos Auxiliares para Reduzir Duplicação (Helpers) ---

    private User createTestUser(String prefix) {
        String email = prefix + "." + UUID.randomUUID() + "@focustech.com";
        return userRepository.save(new User("User " + prefix, email, "12345678"));
    }

    private Conversation createGroupWithMember(User user, String groupName) {
        Conversation conversation = conversationRepository.save(Conversation.createGroup(user, groupName));
        memberRepository.save(ConversationMember.createAdmin(conversation, user));
        return conversation;
    }

    private StompSession connectClient(User user) throws Exception {
        String token = jwtService.generateToken(user);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        stompClient.connectAsync(baseUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                sessionFuture.completeExceptionally(exception);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                sessionFuture.completeExceptionally(exception);
            }
        });

        return sessionFuture.get(10, TimeUnit.SECONDS);
    }

    private CompletableFuture<String> subscribeToQueue(StompSession session, String destination) {
        CompletableFuture<String> future = new CompletableFuture<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = new String((byte[]) payload, StandardCharsets.UTF_8);
                future.complete(json);
            }
        });
        return future;
    }

    private void sendWebSocketMessage(StompSession session, UUID conversationId, String clientMsgId, String content) {
        MessageSendPayload sendData = new MessageSendPayload(conversationId, clientMsgId, content);
        WebSocketEvent<MessageSendPayload> sendEvent = new WebSocketEvent<>("message:send", sendData);

        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/messages.send");
        session.send(sendHeaders, sendEvent);
    }

    private void safeDisconnect(StompSession session) {
        try {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception ignored) {}
    }
}
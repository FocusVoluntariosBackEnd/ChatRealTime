package com.focustecnologia.chat_real_time.message;

import com.focustecnologia.chat_real_time.conversation.entity.Conversation;
import com.focustecnologia.chat_real_time.conversation.entity.ConversationMember;
import com.focustecnologia.chat_real_time.conversation.entity.Role;
import com.focustecnologia.chat_real_time.conversation.entity.Type;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationMemberRepository;
import com.focustecnologia.chat_real_time.conversation.repository.ConversationRepository;
import com.focustecnologia.chat_real_time.message.dto.MessageSendPayload;
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

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    }

    @Test
    void shouldSendMessageAndReceiveAck() throws Exception {
        String email = "sender." + UUID.randomUUID() + "@focustech.com";
        User sender = userRepository.save(new User("Sender", email, "password"));
        Conversation conversation = conversationRepository.save(new Conversation(Type.GROUP, sender));
        memberRepository.save(new ConversationMember(conversation, sender, Role.ADMIN));

        String token = jwtService.generateToken(sender);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();
        String url = "ws://localhost:" + port + "/ws";

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }
        });
        StompSession session = sessionFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<String> ackFuture = new CompletableFuture<>();
        session.subscribe("/user/queue/messages.ack", new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = new String((byte[]) payload, StandardCharsets.UTF_8);
                ackFuture.complete(json);
            }
        });

        String clientMsgId = "msg-" + UUID.randomUUID();
        MessageSendPayload sendData = new MessageSendPayload(conversation.getId(), clientMsgId, "Mensagem enviada com sucesso");
        WebSocketEvent<MessageSendPayload> sendEvent = new WebSocketEvent<>("message:send", sendData);

        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/messages.send");

        session.send(sendHeaders, sendEvent);

        String ackReceived = ackFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(ackReceived);
        assertTrue(ackReceived.contains("message:ack"));
        assertTrue(ackReceived.contains("SAVED"));
        assertTrue(ackReceived.contains(clientMsgId));

        assertTrue(messageRepository.findByConversationIdAndClientMessageId(conversation.getId(), clientMsgId).isPresent());

        session.disconnect();
    }

    @Test
    void shouldBeIdempotentOnDuplicateClientMessageId() throws Exception {
        String email = "sender.idemp." + UUID.randomUUID() + "@focustech.com";
        User sender = userRepository.save(new User("Sender Idempotent", email, "password"));
        Conversation conversation = conversationRepository.save(new Conversation(Type.GROUP, sender));
        memberRepository.save(new ConversationMember(conversation, sender, Role.ADMIN));

        String token = jwtService.generateToken(sender);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();
        String url = "ws://localhost:" + port + "/ws";

        stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                sessionFuture.complete(session);
            }
        });
        StompSession session = sessionFuture.get(5, TimeUnit.SECONDS);

        CompletableFuture<String> firstAckFuture = new CompletableFuture<>();
        session.subscribe("/user/queue/messages.ack", new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = new String((byte[]) payload, StandardCharsets.UTF_8);
                firstAckFuture.complete(json);
            }
        });

        String clientMsgId = "idemp-" + UUID.randomUUID();
        MessageSendPayload sendData = new MessageSendPayload(conversation.getId(), clientMsgId, "Tentativa de envio");
        WebSocketEvent<MessageSendPayload> sendEvent = new WebSocketEvent<>("message:send", sendData);

        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/messages.send");

        session.send(sendHeaders, sendEvent);

        String firstAck = firstAckFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(firstAck);

        CompletableFuture<String> secondAckFuture = new CompletableFuture<>();
        session.subscribe("/user/queue/messages.ack", new StompFrameHandler() {
            @Override
            public java.lang.reflect.Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String json = new String((byte[]) payload, StandardCharsets.UTF_8);
                secondAckFuture.complete(json);
            }
        });

        session.send(sendHeaders, sendEvent);

        String secondAck = secondAckFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(secondAck);
        assertTrue(secondAck.contains("message:ack"));
        assertTrue(secondAck.contains("SAVED"));
        assertTrue(secondAck.contains(clientMsgId));

        session.disconnect();
    }
}

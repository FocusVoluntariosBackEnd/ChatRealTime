package com.focustecnologia.chat_real_time.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceService {
    private static final Duration SESSION_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final SimpUserRegistry simpUserRegistry;
    private final PresenceRedisPublisher presencePublisher;
    private final Map<String, String> localSessions = new ConcurrentHashMap<>();

    public void connected(String userId, String sessionId) {
        cleanupExpiredSessions(userId);
        touch(sessionId, userId);
        localSessions.put(sessionId, userId);

        boolean becameOnline = Long.valueOf(1).equals(
                redis.opsForSet().add(onlineUsersKey(), userId)
        );

        if (becameOnline) {
            presencePublisher.publish(UUID.fromString(userId), PresenceStatus.ONLINE);
        }
    }

    public void disconnected(String sessionId) {
        String userId = localSessions.remove(sessionId);

        if (userId == null) {
            return;
        }

        redis.delete(sessionKey(sessionId));
        redis.opsForSet().remove(userSessionsKey(userId), sessionId);

        removeUserIfOffline(userId);
    }

    @Scheduled(fixedDelay = 20_000)
    public void renewLocalSessionsAndCleanExpiredOnes() {
        localSessions.forEach((sessionId, userId) -> {
            if (isSessionStillConnected(userId, sessionId)) {
                touch(sessionId, userId);
            } else {
                disconnected(sessionId);
            }
        });

        Set<String> onlineUsers = redis.opsForSet().members(onlineUsersKey());

        if (onlineUsers == null) {
            return;
        }

        onlineUsers.forEach(this::removeUserIfOffline);
    }

    private void touch(String sessionId, String userId) {
        redis.opsForValue().set(sessionKey(sessionId), userId, SESSION_TTL);
        redis.opsForSet().add(userSessionsKey(userId), sessionId);
    }

    private void removeUserIfOffline(String userId) {
        if (hasLiveSession(userId)) {
            return;
        }

        redis.delete(userSessionsKey(userId));

        boolean becameOffline = Long.valueOf(1).equals(
                redis.opsForSet().remove(onlineUsersKey(), userId)
        );

        if (becameOffline) {
            presencePublisher.publish(UUID.fromString(userId), PresenceStatus.OFFLINE);
        }
    }

    private void cleanupExpiredSessions(String userId) {
        removeUserIfOffline(userId);
    }

    private boolean isSessionStillConnected(String userId, String sessionId) {
        SimpUser user = simpUserRegistry.getUser(userId);

        return user != null && user.getSessions().stream()
                .anyMatch(session -> session.getId().equals(sessionId));
    }

    private boolean hasLiveSession(String userId) {
        Set<String> sessionIds = redis.opsForSet().members(userSessionsKey(userId));

        if (sessionIds == null || sessionIds.isEmpty()) {
            return false;
        }

        boolean hasLiveSession = false;

        for (String sessionId : sessionIds) {
            if (Boolean.TRUE.equals(redis.hasKey(sessionKey(sessionId)))) {
                hasLiveSession = true;
            } else {
                redis.opsForSet().remove(userSessionsKey(userId), sessionId);
            }
        }

        return hasLiveSession;
    }

    private String sessionKey(String sessionId) {
        return "chat:presence:session:" + sessionId;
    }

    private String userSessionsKey(String userId) {
        return "chat:presence:user:" + userId + ":sessions";
    }

    private String onlineUsersKey() {
        return "chat:presence:users";
    }
}

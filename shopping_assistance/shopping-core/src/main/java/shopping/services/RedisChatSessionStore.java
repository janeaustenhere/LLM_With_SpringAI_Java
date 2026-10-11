package shopping.services;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import shopping.config.ShoppingProperties;
import shopping.domain.ChatHistoryMessage;
import shopping.domain.ChatSessionResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RedisChatSessionStore implements ChatSessionStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ShoppingProperties properties;

    public RedisChatSessionStore(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            ShoppingProperties properties
    ) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public ChatSessionResponse createSession(
            String userId,
            String title
    ) {
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();

        ChatSessionResponse session = new ChatSessionResponse(
                sessionId,
                title,
                now,
                now
        );

        redis.opsForZSet().add(
                sessionsKey(userId),
                sessionId.toString(),
                now.toEpochMilli()
        );

        redis.opsForHash().put(
                titlesKey(userId),
                sessionId.toString(),
                title
        );

        redis.opsForHash().put(
                createdAtKey(userId),
                sessionId.toString(),
                now.toString()
        );

        refreshExpiry(userId, sessionId);

        return session;
    }

    @Override
    public List<ChatSessionResponse> listSessions(String userId) {
        Set<String> ids = redis.opsForZSet().reverseRange(
                sessionsKey(userId),
                0,
                -1
        );

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<ChatSessionResponse> result = new ArrayList<>();

        for (String id : ids) {
            String title = (String) redis.opsForHash().get(
                    titlesKey(userId),
                    id
            );

            String createdAt = (String) redis.opsForHash().get(
                    createdAtKey(userId),
                    id
            );

            Double updatedAt = redis.opsForZSet().score(
                    sessionsKey(userId),
                    id
            );

            if (title == null || updatedAt == null) {
                continue;
            }

            result.add(
                    new ChatSessionResponse(
                            UUID.fromString(id),
                            title,
                            createdAt == null
                                    ? Instant.ofEpochMilli(
                                    updatedAt.longValue()
                            )
                                    : Instant.parse(createdAt),
                            Instant.ofEpochMilli(updatedAt.longValue())
                    )
            );
        }

        return result;
    }

    @Override
    public boolean sessionExists(
            String userId,
            UUID sessionId
    ) {
        return redis.opsForZSet().score(
                sessionsKey(userId),
                sessionId.toString()
        ) != null;
    }

    @Override
    public List<ChatHistoryMessage> getHistory(
            String userId,
            UUID sessionId
    ) {
        List<String> values = redis.opsForList().range(
                historyKey(userId, sessionId),
                0,
                -1
        );

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .map(this::deserialize)
                .toList();
    }

    @Override
    public void appendMessage(
            String userId,
            UUID sessionId,
            ChatHistoryMessage message
    ) {
        String historyKey = historyKey(userId, sessionId);
        Instant now = Instant.now();

        redis.opsForList().rightPush(
                historyKey,
                serialize(message)
        );

        redis.opsForZSet().add(
                sessionsKey(userId),
                sessionId.toString(),
                now.toEpochMilli()
        );

        refreshExpiry(userId, sessionId);
    }

    @Override
    public void deleteSession(
            String userId,
            UUID sessionId
    ) {
        String id = sessionId.toString();

        redis.delete(historyKey(userId, sessionId));
        redis.opsForZSet().remove(sessionsKey(userId), id);
        redis.opsForHash().delete(titlesKey(userId), id);
        redis.opsForHash().delete(createdAtKey(userId), id);
    }

    private void refreshExpiry(
            String userId,
            UUID sessionId
    ) {
        var ttl = properties.chatHistoryTtl();

        redis.expire(sessionsKey(userId), ttl);
        redis.expire(titlesKey(userId), ttl);
        redis.expire(createdAtKey(userId), ttl);
        redis.expire(historyKey(userId, sessionId), ttl);
    }

    private String sessionsKey(String userId) {
        return "%s:sessions:{%s}".formatted(
                properties.redisKeyPrefix(),
                userId
        );
    }

    private String titlesKey(String userId) {
        return "%s:session-titles:{%s}".formatted(
                properties.redisKeyPrefix(),
                userId
        );
    }

    private String createdAtKey(String userId) {
        return "%s:session-created:{%s}".formatted(
                properties.redisKeyPrefix(),
                userId
        );
    }

    private String historyKey(
            String userId,
            UUID sessionId
    ) {
        return "%s:history:{%s}:%s".formatted(
                properties.redisKeyPrefix(),
                userId,
                sessionId
        );
    }

    private String serialize(ChatHistoryMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        }
        catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not serialize chat history",
                    exception
            );
        }
    }

    private ChatHistoryMessage deserialize(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    ChatHistoryMessage.class
            );
        }
        catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Could not deserialize chat history",
                    exception
            );
        }
    }
}

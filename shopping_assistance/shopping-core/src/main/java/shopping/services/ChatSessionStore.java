package shopping.services;

import shopping.domain.ChatHistoryMessage;
import shopping.domain.ChatSessionResponse;

import java.util.List;
import java.util.UUID;

public interface ChatSessionStore {

    ChatSessionResponse createSession(
            String userId,
            String title
    );

    List<ChatSessionResponse> listSessions(String userId);

    boolean sessionExists(
            String userId,
            UUID sessionId
    );

    List<ChatHistoryMessage> getHistory(
            String userId,
            UUID sessionId
    );

    void appendMessage(
            String userId,
            UUID sessionId,
            ChatHistoryMessage message
    );

    void deleteSession(
            String userId,
            UUID sessionId
    );
}
package shopping.controller;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import shopping.domain.*;
import shopping.services.ChatSessionStore;
import shopping.services.IShopGateway;
import shopping.services.IShoppingAgent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final IShopGateway shopGateway;
    private final IShoppingAgent shoppingAgent;
    private final ChatSessionStore chatSessionStore;

    public ChatController(
            IShopGateway shopGateway,
            IShoppingAgent shoppingAgent,
            ChatSessionStore chatSessionStore
    ) {
        this.shopGateway = shopGateway;
        this.shoppingAgent = shoppingAgent;
        this.chatSessionStore = chatSessionStore;
    }

    @GetMapping
    public Map<String,String> health(){
        return Map.of("health", "ok");
    }

    @PostMapping("/auth/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request){

        return shopGateway.login(request.email().strip());
    }

    @PostMapping("/chat")
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    ) {
        String token = extractBearerToken(authorization);
        String userId = hash(token);
        UUID sessionId = request.sessionId();

        requireOwnedSession(userId, sessionId);

        String conversationId =
                userId + ":" + sessionId;

        String query = request.query().strip();
        Instant now = Instant.now();

        String answer = shoppingAgent.chat(
                query,
                token,
                conversationId
        );

        // Save visible history only after the AI call succeeds.
        chatSessionStore.appendMessage(
                userId,
                sessionId,
                new ChatHistoryMessage(
                        "user",
                        query,
                        now
                )
        );

        chatSessionStore.appendMessage(
                userId,
                sessionId,
                new ChatHistoryMessage(
                        "assistant",
                        answer,
                        Instant.now()
                )
        );

        return new ChatResponse(answer);
    }

    @DeleteMapping("/chat/history")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearHistory(
            @RequestParam
            String authorization
    ){

        String token = extractBearerToken(authorization);
        shoppingAgent.clear(hash(token));
    }

    @PostMapping("/chat/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatSessionResponse createSession(
            @Valid @RequestBody CreateChatSessionRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    ) {
        String token = extractBearerToken(authorization);
        String userId = hash(token);

        return chatSessionStore.createSession(
                userId,
                request.title().strip()
        );
    }

    @GetMapping("/chat/sessions")
    public List<ChatSessionResponse> listSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    ) {
        String token = extractBearerToken(authorization);
        return chatSessionStore.listSessions(hash(token));
    }

    @GetMapping("/chat/sessions/{sessionId}/history")
    public List<ChatHistoryMessage> getHistory(
            @PathVariable UUID sessionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    ) {
        String userId = hash(
                extractBearerToken(authorization)
        );

        requireOwnedSession(userId, sessionId);

        return chatSessionStore.getHistory(
                userId,
                sessionId
        );
    }

    @DeleteMapping("/chat/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(
            @PathVariable UUID sessionId,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization
    ) {
        String userId = hash(
                extractBearerToken(authorization)
        );

        requireOwnedSession(userId, sessionId);

        String conversationId =
                userId + ":" + sessionId;

        shoppingAgent.clear(conversationId);
        chatSessionStore.deleteSession(userId, sessionId);
    }

    private String extractBearerToken(String authorization){

        if(authorization == null || !authorization.startsWith("Bearer ")
        || authorization.length() <= 7){

            throw  new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "A Bearer token is required ");
        }

        return authorization.substring(7).trim();
    }

    private String hash(String value){

        try{

            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void requireOwnedSession(
            String userId,
            UUID sessionId
    ) {
        if (!chatSessionStore.sessionExists(userId, sessionId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Chat session not found"
            );
        }
    }




}

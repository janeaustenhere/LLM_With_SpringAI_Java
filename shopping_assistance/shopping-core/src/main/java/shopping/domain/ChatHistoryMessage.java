package shopping.domain;

import java.time.Instant;

public record ChatHistoryMessage(
        String role,
        String content,
        Instant createdAt
) {
}
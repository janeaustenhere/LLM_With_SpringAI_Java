package shopping.domain;

import java.time.Instant;
import java.util.UUID;

public record ChatSessionResponse(
        UUID sessionId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
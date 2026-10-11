package shopping.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(
        @NotNull
        UUID sessionId,

        @NotBlank
        @Size(max = 4000)
        String query
) {
}
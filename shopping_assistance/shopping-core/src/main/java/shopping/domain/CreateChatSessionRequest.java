package shopping.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatSessionRequest(
        @NotBlank
        @Size(max = 100)
        String title
) {
}
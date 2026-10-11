package shopping.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,
        Map<String, Object> customer
) {
}

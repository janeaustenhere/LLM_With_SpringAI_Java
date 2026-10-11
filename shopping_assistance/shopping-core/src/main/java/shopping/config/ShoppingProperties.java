package shopping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "shopping")
public record ShoppingProperties(
        String shopApiUrl,

        @DefaultValue("30s")
        Duration shopApiTimeout,

        @DefaultValue("20")
        int maxHistoryMessages,

        @DefaultValue("shop")
        String redisKeyPrefix,

        @DefaultValue("30d")
        Duration chatHistoryTtl
) {
}

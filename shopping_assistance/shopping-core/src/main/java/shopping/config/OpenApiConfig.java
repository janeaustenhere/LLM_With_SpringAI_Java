package shopping.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI shoppingOpenApi() {
        // 1. Define how the scheme operates (HTTP Bearer / JWT)
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 2. Bind the exact string identifier to your application pathways
        SecurityRequirement bearerRequirement = new SecurityRequirement()
                .addList(BEARER_AUTH);

        return new OpenAPI()
                .info(new Info()
                        .title("Shopping Assistant API")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerScheme))
                // 3. Apply it globally so Swagger UI knows to inject it into every request
                .addSecurityItem(bearerRequirement);
    }
}

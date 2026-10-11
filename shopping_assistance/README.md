# Shopping Assistant

A Java 21 multi-module shopping assistant built with Spring Boot. The project provides two independently runnable AI implementations—Spring AI and LangChain4j—over the same REST API, Shop API gateway, Swagger documentation, and lightweight browser chat UI.

## Supported capabilities

The assistant is intentionally limited to:

- viewing the signed-in customer's cart;
- listing orders or reading an existing order;
- cancelling an item from an existing order after validating real order and item IDs.

It must not place orders, perform checkout or payment, modify the cart, issue refunds or exchanges, or change customer/account information. These operations are not exposed as tools. The shared system prompt also instructs both implementations to refuse unsupported operations and never claim an action succeeded without a successful tool result.

## Modules

```text
shopping_assistance/
├── pom.xml
├── shopping-core/
│   ├── shared controllers and DTOs
│   ├── Shop API gateway
│   ├── shared interfaces and system prompt
│   ├── Swagger/OpenAPI configuration
│   └── static browser chat UI
├── spring-ai-app/
│   └── Spring AI agent, tools, memory and application
└── langchain4j-app/
    └── LangChain4j agent, tools, memory and application
```

`shopping-core` is a library and is not runnable by itself. Choose either `spring-ai-app` or `langchain4j-app` as the application.

## Prerequisites

- Java 21
- Maven Wrapper included in the repository
- An OpenRouter API key
- A running Shop API, locally or remotely
- An OpenRouter model that supports tool/function calling

## Configuration

Set these environment variables before starting an application:

```bash
export OPENROUTER_API_KEY="your-openrouter-key"
export OPENROUTER_MODEL="openai/gpt-4o-mini"
export OPENROUTER_BASE_URL="https://openrouter.ai/api/v1"
export SHOP_API_URL="http://localhost:8080"
```

Optional variables:

```bash
export SERVER_PORT="8090"
export SHOP_API_TIMEOUT="30s"
export MAX_HISTORY_MESSAGES="20"
export LLM_TIMEOUT="60s"
```

The Spring AI application currently defines its OpenRouter URL directly in `spring-ai-app/src/main/resources/application.yaml`; `OPENROUTER_BASE_URL` is used by the LangChain4j application. Spring AI 2.x uses the official OpenAI Java SDK, so the OpenAI-compatible base URL must include `/v1`.

## Build

Build every module:

```bash
./mvnw clean package
```

Compile without running tests:

```bash
./mvnw -DskipTests compile
```

## Run with Spring AI

```bash
./mvnw -pl spring-ai-app -am spring-boot:run
```

Default URLs:

- Chat UI: <http://localhost:8090/>
- Swagger UI: <http://localhost:8090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8090/v3/api-docs>

## Run with LangChain4j

```bash
./mvnw -pl langchain4j-app -am spring-boot:run
```

Default URLs:

- Chat UI: <http://localhost:8091/>
- Swagger UI: <http://localhost:8091/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8091/v3/api-docs>

The two applications use different ports and can run simultaneously.

## API workflow

### 1. Sign in

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "customer@example.com"
}
```

The response contains `access_token` and customer information.

### 2. Chat

The current development controller accepts authorization as a request parameter:

```http
POST /api/v1/chat?authorization=Bearer%20<access-token>
Content-Type: application/json

{
  "query": "Show my current cart"
}
```

### 3. Clear conversation memory

```http
DELETE /api/v1/chat/history?authorization=Bearer%20<access-token>
```

Conversation IDs are derived from a SHA-256 hash of the Shop API token. Memory is currently stored in-process and is lost whenever the application restarts.

## Testing from Swagger

1. Open the Swagger URL for the selected application.
2. Call `POST /api/v1/auth/login`.
3. Copy the returned `access_token`.
4. Call `POST /api/v1/chat` and provide `Bearer <access-token>` as the `authorization` request parameter.
5. Try messages such as:

```text
Show my cart.
List my orders.
Show details for my latest order.
Cancel one unit of the selected order item.
```

An unsupported request such as `Place an order for my cart` should be refused.

## Browser UI

The shared UI is located at:

```text
shopping-core/src/main/resources/static/index.html
```

It signs in through the shared REST API, stores the development token in browser `localStorage`, sends chat messages, and clears server-side conversation memory on sign-out. No separate Node.js or frontend server is required.

## Security notes

This project is currently configured for local learning and experimentation.

- Passing an access token as a query parameter can expose it through browser history, access logs, proxies and monitoring tools. Production applications should use the standard `Authorization: Bearer <token>` header.
- Storing access tokens in `localStorage` is convenient for this demo but is vulnerable to token theft if an XSS flaw is introduced.
- `InsecureRestClientConfig` disables TLS certificate verification. It should be removed before using real customer data or deploying the service. The preferred solution is a valid certificate and a correctly functioning Java TLS/network path.
- In-memory chat memory is neither durable nor suitable for horizontally scaled instances.
- Tool absence is the final enforcement boundary: unsupported mutation tools must never be added without authorization and validation controls.

## Troubleshooting

### No operations defined in Swagger

Ensure the application scans both package roots:

```java
@SpringBootApplication(scanBasePackages = {"com.shopping", "shopping"})
```

Then reload Maven and restart the application.

### No static resource for `/`

Confirm the UI exists at:

```text
shopping-core/src/main/resources/static/index.html
```

Run `./mvnw clean package` so it is copied into the core JAR.

### OpenRouter returns 404 for `Hi`

Use the Spring AI 2.x base URL:

```text
https://openrouter.ai/api/v1
```

### Vercel TLS handshake is terminated

If Python and `curl` work but Java fails before receiving a certificate, certificate validation is not the cause. For development, run the Shop API locally or test from another network. Do not retain trust-all TLS configuration as a production workaround.

## Technology versions

- Java 21
- Spring Boot 4.0.3
- Spring AI 2.0.1
- LangChain4j 1.22.0-beta32
- springdoc-openapi 3.1.1

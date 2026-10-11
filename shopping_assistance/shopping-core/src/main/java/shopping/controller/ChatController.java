package shopping.controller;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import shopping.domain.ChatRequest;
import shopping.domain.ChatResponse;
import shopping.domain.LoginRequest;
import shopping.domain.LoginResponse;
import shopping.services.IShopGateway;
import shopping.services.IShoppingAgent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private final IShopGateway shopGateway;
    private final IShoppingAgent shoppingAgent;


    public ChatController(IShopGateway shopGateway, IShoppingAgent shoppingAgent) {
        this.shopGateway = shopGateway;
        this.shoppingAgent = shoppingAgent;
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
            @RequestParam
            String authorization
            ){

        String token = extractBearerToken(authorization);
        String conversationId = hash(token);

        String answer = shoppingAgent.chat(
                request.query().strip(),
                token,
                conversationId
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




}

package shopping.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import shopping.config.ShoppingProperties;
import shopping.domain.LoginResponse;
import shopping.exceptions.ShopApiException;

import java.net.http.HttpHeaders;
import java.util.Map;
import java.util.UUID;

@Component
public class HTTPShopGateway implements IShopGateway{

    private static final ParameterizedTypeReference<Map<String,Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    private final RestClient restClient;

    public HTTPShopGateway(
            RestClient.Builder builder,
            ShoppingProperties properties
    ) {
        this.restClient = builder
                .clone()
                .baseUrl(properties.shopApiUrl())
                .build();
    }
    @Override
    public LoginResponse login(String email) {
        try{
            return restClient.post()
                    .uri("/auth/login")
                    .body(Map.of("email", email))
                    .retrieve()
                    .body(LoginResponse.class);
        } catch (RestClientException exception){
            throw new ShopApiException(exception.getMessage());
        }
    }

    @Override
    public Map<String, Object> getCart(String token) {
        try{
            return restClient.get()
                    .uri("/cart")
                    .header("Authorization", "Bearer "+token)
                    .retrieve()
                    .body(MAP_TYPE);
        } catch (RestClientException exception){

            throw  new ShopApiException(exception.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOrder(String token, String orderId) {
       try{
           String path = orderId == null || orderId.isBlank()
                   ? "/orders" : "orders/{orderId}";

           RestClient.RequestHeadersSpec<?> request =
                   orderId == null || orderId.isBlank() ?
                           restClient.get().uri(path) : restClient.get().uri(path,orderId);

           return request.header("Authorization", "Bearer " + token)
                   .retrieve()
                   .body(MAP_TYPE);
       } catch (RestClientException e) {
           throw new ShopApiException(e.getMessage());
       }
    }

    @Override
    public Map<String, Object> cancelItem(String token, String orderId, String itemId, int quantity) {
        try{
            return  restClient.post()
                    .uri("/order/{orderId}/{items}/{itemId}/cancel",
                    orderId, itemId)
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of(
                            "quantity", quantity,
                            "operation_id", UUID.randomUUID().toString()
                    ))
                    .retrieve()
                    .body(MAP_TYPE);
        }catch (RestClientException exception){
            throw new ShopApiException(exception.getMessage());
        }
    }
}

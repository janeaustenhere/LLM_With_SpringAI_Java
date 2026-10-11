package shopping.services;

import shopping.domain.LoginResponse;

import java.util.Map;

public interface IShopGateway {

    LoginResponse login(String email);

    Map<String, Object> getCart(String token);

    Map<String,Object> getOrder(String token, String orderId);

    Map<String, Object> cancelItem(String token, String orderId, String itemId,
                                   int quantity);
}

package shopping.services;

public interface IShoppingAgent {

    String chat(String query, String shopToken, String conversationId);

    void clear(String conversationId);
}

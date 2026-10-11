package com.shopping.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import shopping.services.IShopGateway;

import java.util.Map;

public class LangChain4jShoppingTools {

    private final IShopGateway shopGateway;
    private final String token;

    public LangChain4jShoppingTools(IShopGateway shopGateway, String token) {
        this.shopGateway = shopGateway;
        this.token = token;
    }

    @Tool("""
            Read the signed-in customer's current cart,
            including , quantites and totals
            """)
    public Map<String,Object> getCart(){
        return shopGateway.getCart(token);
    }

    @Tool("""
            List the signed-in customer's order or retrieve one order.
            Call this before cancellation and never guess an order Id.
            """)
    public Map<String,Object> getOrders(
            @P(
                    value = """
                            Real Order Id returned by getOrders.
                            Omit it to list all orders.
                            """,
                    required = false
            ) String orderId
    ) {
        return shopGateway.getOrder(token, orderId);
    }

    @Tool("""
            Cancel an explicitly requested quantity of an order item 
            Call getOrders first and use only verified Ids.
            """)
    public Map<String,Object> cancelItem(
            @P("Verified read Order Id")
            String orderId,

            @P("Verified real item Id")
            String itemId,

            @P("Quantity to cancel, from 1 to 99")
            int quantity

    ){
        if(quantity < 1 || quantity > 99){

            throw new IllegalArgumentException(" quantity must be between 1 and 99");
        }

        return shopGateway.cancelItem(token, orderId, itemId, quantity);
    }
}


package com.shopping.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import shopping.services.IShopGateway;

import java.util.Map;

public class SpringAiShoppingTools {

    private final IShopGateway shopGateway;
    private final String token;


    public SpringAiShoppingTools(IShopGateway shopGateway, String token) {
        this.shopGateway = shopGateway;
        this.token = token;
    }

    @Tool(
            name = "getCart",
            description = """
                    Read the signed-in customer's current cart
                    including items, quantites and totals
                    """
    )
    public Map<String,Object> getCart(){
        return shopGateway.getCart(token);
    }

    @Tool(
            name = "getOrders",
            description = """
            List the signed-in customer's orders or retrieve one order.
            Call this before attempting a cancellation.
            Never guess an order Id. 
            """
    )
    public Map<String,Object> getOrders(
            @ToolParam(description = """
                    Real order Id previously returned by this tool
                    omit it to list all orders.
                    """,
            required = false) String orderId
    ){
        return shopGateway.getOrder(token, orderId);
    }

    @Tool(
            name = "cancelItem",
            description = """
                    Cancel an explicitly requested quantity of an order item.
                    Call getOrders first and use only verified IDs.
                    """
    )
    public Map<String,Object> cancelItem(
            @ToolParam(description = "Verified read order Id")
            String orderId,
            @ToolParam(description = "Verified real item id")
            String itemId,
            @ToolParam(description = "Quantity to cancel, from 1 to 99")
            int quantity
    ){
        if (quantity < 1 || quantity > 99){
            throw  new IllegalArgumentException("quantity must be between 1 and 99");
        }

        return shopGateway.cancelItem(token,orderId,itemId, quantity);
    }
}

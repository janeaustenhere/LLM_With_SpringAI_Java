package com.shopping.agent;

import com.shopping.tools.SpringAiShoppingTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import shopping.services.IShopGateway;
import shopping.services.IShoppingAgent;

@Component
public class ShoppingAgent implements IShoppingAgent {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final IShopGateway shopGateway;

    public ShoppingAgent(ChatClient chatClient, ChatMemory chatMemory, IShopGateway shopGateway) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.shopGateway = shopGateway;
    }


    @Override
    public String chat(String query, String shopToken, String conversationId) {

        SpringAiShoppingTools tools = new SpringAiShoppingTools(shopGateway, shopToken);

        return chatClient.prompt()
                .user(query)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,
                        conversationId))
                .tools(tools)
                .call()
                .content();
    }

    @Override
    public void clear(String conversationId) {
        chatMemory.clear(conversationId);
    }
}

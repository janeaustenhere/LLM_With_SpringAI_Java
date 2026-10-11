package com.shopping.services;

import com.shopping.LangChain4jMemoryRegistry;
import com.shopping.tools.LangChain4jShoppingTools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import org.springframework.stereotype.Service;
import shopping.services.IShopGateway;
import shopping.services.IShoppingAgent;

@Service
public class LangChain4jAgent implements IShoppingAgent {

    private final ChatModel chatModel;
    private final IShopGateway shopGateway;
    private final LangChain4jMemoryRegistry langChain4jMemoryRegirstry;

    public LangChain4jAgent(ChatModel chatModel, IShopGateway shopGateway, LangChain4jMemoryRegistry langChain4jMemoryRegirstry) {
        this.chatModel = chatModel;
        this.shopGateway = shopGateway;
        this.langChain4jMemoryRegirstry = langChain4jMemoryRegirstry;
    }

    @Override
    public String chat(String query, String shopToken, String conversationId) {
        LangChain4jShoppingTools tools = new LangChain4jShoppingTools(shopGateway, shopToken);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(langChain4jMemoryRegirstry :: get)
                .tools(tools)
                .build();

        return assistant.chat(conversationId, query);
    }

    @Override
    public void clear(String conversationId) {
        langChain4jMemoryRegirstry.clear(conversationId);
    }
}

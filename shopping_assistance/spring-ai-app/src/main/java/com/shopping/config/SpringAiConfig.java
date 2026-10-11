package com.shopping.config;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shopping.config.ShoppingProperties;
import shopping.domain.ShoppingPrompt;

@Configuration
public class SpringAiConfig {

    @Bean
    ChatMemory chatMemory(ShoppingProperties properties){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(properties.maxHistoryMessages())
                .build();
    }

    @Bean
    ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory){

        return ChatClient.builder(chatModel)
                .defaultSystem(ShoppingPrompt.SYSTEM)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }
}

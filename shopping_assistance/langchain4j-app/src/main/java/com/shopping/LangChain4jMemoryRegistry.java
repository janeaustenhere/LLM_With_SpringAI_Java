package com.shopping;


import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;
import shopping.config.ShoppingProperties;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LangChain4jMemoryRegistry {

    private final ConcurrentMap<String, ChatMemory> memories = new ConcurrentHashMap<>();

    private final int maxMessages;


    public LangChain4jMemoryRegistry(ShoppingProperties shoppingProperties) {
        this.maxMessages = shoppingProperties.maxHistoryMessages();
    }

    public ChatMemory get(Object memoryId){
        String id = String.valueOf(memoryId);

        return memories.computeIfAbsent(id,
                ignored -> MessageWindowChatMemory.builder()
                        .maxMessages(maxMessages).build());

    }

    public  void clear(String memoryId){
        ChatMemory memory = memories.remove(memoryId);

        if(memory != null){
            memory.clear();
        }
    }
}

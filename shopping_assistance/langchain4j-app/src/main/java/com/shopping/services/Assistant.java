package com.shopping.services;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import shopping.domain.ShoppingPrompt;

public interface Assistant {

    @SystemMessage(ShoppingPrompt.SYSTEM)
    String chat(@MemoryId String conversationId,
                @UserMessage String query);
}

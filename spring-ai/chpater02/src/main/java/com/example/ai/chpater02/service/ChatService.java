package com.example.ai.chpater02.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String getChatResponse(String query) {
        return chatClient.prompt()
                .user(query)
                .call()
                .content();
    }
}

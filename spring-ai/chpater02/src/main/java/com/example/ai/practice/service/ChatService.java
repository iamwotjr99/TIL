package com.example.ai.practice.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String getExpressResponse(String query) {
        String queryStrTemplate = "당신은 운동 플래너 전문가입니다. " + "항상 사용자의 운동을 텍스트로 정리하세요. "
                + "이제 사용자의 운동을 정리해서 답변해주세요: {query}";

        return chatClient.prompt()
                .user(u -> u.text(queryStrTemplate).param("query", query))
                .call()
                .content();
    }

}

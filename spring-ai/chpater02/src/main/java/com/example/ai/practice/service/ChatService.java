package com.example.ai.practice.service;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
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

    public String getExplicitTemplateResponse(String subject, String example) {
        PromptTemplate strTemplate = PromptTemplate.builder()
                .template("{subject} 주제에서, {example} 예시를 들어주세요.")
                .build();

        String renderedMessage = strTemplate.render(Map.of("subject", subject, "example", example));

        Prompt prompt = new Prompt(renderedMessage);
        return this.chatClient.prompt(prompt).call().content();
    }

    public String getRoleBasedTemplateResponse(String subject, String example) {
        String systemText = "당신은 {subject} 전문가입니다. 항상 전문적인 관점에서 답변해 주세요.";
        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemTemplate.createMessage(Map.of("subject", subject));

        String userText = "그것과 관련하여 {example}을(를) 예제와 함계 설명해주세요.";
        PromptTemplate userTemplate = new PromptTemplate(userText);
        Message userMessage = userTemplate.createMessage(Map.of("example", example));

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return this.chatClient.prompt(prompt).call().content();
    }
}

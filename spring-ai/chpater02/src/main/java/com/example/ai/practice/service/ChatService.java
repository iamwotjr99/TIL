package com.example.ai.practice.service;

import com.example.ai.practice.entity.Tutorial;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient openAiClient;
    private final ChatClient ollamaClient;

    public ChatService(
            @Qualifier("openAiChatClient") ChatClient openAiClient,
            @Qualifier("ollamaChatClient") ChatClient ollamaClient) {
        this.openAiClient = openAiClient;
        this.ollamaClient = ollamaClient;
    }

    public String getOpenAiResponse(String query) {
        return openAiClient.prompt()
                .user(query)
                .call()
                .content();
    }

    public String chatWithSystemRole(String query) {
        return openAiClient.prompt()
                .system("스포츠 전문가로서 답해주세요.")
                .user(query)
                .call()
                .content();
    }

    public String simpleChat(String query) {
        Prompt prompt = new Prompt(query);
        return openAiClient.prompt(prompt)
                .call()
                .content();
    }

    public String getDetailedContent(String query) {
        Prompt prompt = new Prompt(query);
        String text = openAiClient.prompt(prompt)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        System.out.println(text);

        return text;
    }

    public String getMetaData(String query) {
        Prompt prompt = new Prompt(query);
        ChatResponseMetadata metadata = openAiClient.prompt(prompt)
                .call()
                .chatResponse()
                .getMetadata();

        System.out.println(metadata);

        return metadata.toString();
    }

    public Tutorial getEntity(String query) {
        Prompt prompt = new Prompt(query);
        return openAiClient.prompt(prompt)
                .call()
                .entity(Tutorial.class);
    }

    public List<String> getStringList(String query) {
        return openAiClient.prompt()
                .user(query)
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    public List<Tutorial> getTutorialList(String query) {
        return openAiClient.prompt()
                .user(query)
                .call()
                .entity(new ParameterizedTypeReference<List<Tutorial>>() {});
    }

    public String getPriorityTestResponse(String query) {
        OpenAiChatOptions requestOptions = OpenAiChatOptions.builder()
                .temperature(1.0)
                .build();

        return openAiClient.prompt(new Prompt(query, requestOptions))
                .call()
                .content();
    }

    public String getOllamaResponse(String query) {
        return ollamaClient.prompt()
                .user(query)
                .call()
                .content();
    }
}

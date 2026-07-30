package com.example.ai.chpater02.controller;

import com.example.ai.chpater02.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam(value = "query", defaultValue = "안녕") String query) {
        String chatResponse = chatService.getChatResponse(query);
        return ResponseEntity.ok(chatResponse);
    }
}

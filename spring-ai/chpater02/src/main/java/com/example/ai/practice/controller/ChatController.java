package com.example.ai.practice.controller;

import com.example.ai.practice.service.ChatService;
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

    @GetMapping("/chat/template")
    public ResponseEntity<String> chatWithTemplate(@RequestParam String query) {
        String response = chatService.getExpressResponse(query);
        return ResponseEntity.ok(response);
    }
}

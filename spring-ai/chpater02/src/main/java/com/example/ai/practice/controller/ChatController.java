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

    @GetMapping("/chat/explicit")
    public ResponseEntity<String> chatWithExplicitTemplate(
            @RequestParam(defaultValue = "Spring Framework") String subject,
            @RequestParam(defaultValue = "Spring @Controller example") String example
    ) {
        String response = chatService.getExplicitTemplateResponse(subject, example);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat/role-dynamic")
    public ResponseEntity<String> chatWithRoleTemplate(
            @RequestParam(defaultValue = "자바") String subject,
            @RequestParam(defaultValue = "람다 스트림") String example
    ) {
        String response = chatService.getRoleBasedTemplateResponse(subject,
                example);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat/external")
    public ResponseEntity<String> chatWithExternalTemplate(
            @RequestParam(defaultValue = "Spring Framework validation") String concept
    ) {
        String response = chatService.getExternalTemplateResponse(concept);

        return ResponseEntity.ok(response);
    }
}

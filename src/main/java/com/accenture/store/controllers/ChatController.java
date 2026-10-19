//member 4
package com.accenture.store.controllers;

import com.accenture.store.dtos.ChatRequest;
import com.accenture.store.dtos.ChatResponse;
import com.accenture.store.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public shopping assistant. The endpoint is read-only: it looks products up
 * and asks the AI provider for an answer, and it never changes store data.
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.getMessage());
    }
}

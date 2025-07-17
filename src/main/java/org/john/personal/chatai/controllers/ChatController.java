package org.john.personal.chatai.controllers;

import org.john.personal.chatai.services.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
@RequestMapping("/api/chat")
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(final ChatService chatService) {
        this.chatService = chatService;
    }

    /*
    * Chat with context
    * POST /api/simple
    * */
    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request){
        String message = request.get("message");
        String conversationId = request.get("conversationId");

        if(conversationId == null || conversationId.isEmpty()){
            conversationId = "default-conversation";
        }
        System.out.println("\n Starting chat request...");

        String response = chatService.chatWithContextMessageMemory(message, conversationId);

        return ResponseEntity.ok(Map.of(
                "userMessage", message,
                "aiResponse", response,
                "conversationId", conversationId,
                "timeStamp", new Date().toString()));
    }
}

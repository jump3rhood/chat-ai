package org.john.personal.chatai.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ChatController {

    private final OpenAiChatModel openAiChatModel;
    private final OllamaChatModel ollamaChatModel;

    public ChatController(OpenAiChatModel openAiChatModel, OllamaChatModel ollamaChatModel) {
        this.openAiChatModel = openAiChatModel;
        this.ollamaChatModel = ollamaChatModel;
    }

    @PostMapping("/chat/ollama")
    public Map<String, Object> ollamaChat(@RequestBody Map<String, String> request) {
        String userInput = request.get("message");

        ChatClient client = ChatClient.builder(ollamaChatModel).build();
        String response = client
                .prompt()
                .user(userInput)
                .call()
                .content();
        return Map.of("provider", "ollama", "answer", response);
    }
    @PostMapping("/chat/openai/")
    public Map<String, Object> openAIChat(@RequestBody Map<String, String> request) {
        String userInput = request.get("message");
        ChatClient client = ChatClient.builder(openAiChatModel).build();
        String response = client
                .prompt()
                .user(userInput)
                .call()
                .content();
        return Map.of("provider", "openai", "answer", response);
    }
}

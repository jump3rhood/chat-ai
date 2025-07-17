package org.john.personal.chatai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final ChatClient chatClient;

    // in-memory conversation for context storage
    private final Map<String, List<String>> conversations = new HashMap<String, List<String>>();

    public ChatService(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.builder(ollamaChatModel).build();
    }

    /*
    * Chat with context
    * */
    public String chatWithContext(String userMessage, String conversationId){

        System.out.println("===Simple Context Chat ===");
        System.out.println("User message: " + userMessage);
        System.out.println("Conversation ID: " + conversationId);

        // 1. Get existing conversation or create a new one
        List<String> conversation = conversations.get(conversationId);
        if(conversation == null){
            conversation = new ArrayList<>();
            conversations.put(conversationId, conversation);
            System.out.println("Created new conversation History with " + conversationId);
        } else{
            System.out.println("Found existing conversation with " + conversation.size() + "messages.");
        }
        // 2. Added user message to History
        conversation.add("User: " + userMessage);

        // 3. Build Context
        String context = buildContextString(conversation);

        String aiResponse = chatClient
                .prompt(getSimpleChatContextPrompt())
                .user(context)
                .call()
                .content();

        conversation.add("AI: " + aiResponse);
        System.out.println("AI: " + aiResponse);

        System.out.println("Current conversation has " + conversation.size() + "messages.");

        return aiResponse;
    }

    private String buildContextString(List<String> conversation) {
        System.out.println("=== Building Context String ===");
        if(conversation.isEmpty()){
            System.out.println("No previous messages");
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("Previous conversation: \n");
        for(int i = 0; i< conversation.size(); i++){
            String message = conversation.get(i);
            context.append(message).append("\n");
            System.out.println("Added to context: " + message);
        }

        context.append("\nPlease respond to the conversation above.");
        System.out.println("Final context string length: " + context.length());
        return context.toString();
    }
    public Prompt getSimpleChatContextPrompt(){
        return new Prompt("""
                You are a helpful AI chatbot that is interacting with a user.
                The text you received contains the previous history of the conversation so far. 
                The "User:" is what the user had asked before and "AI Response:" is what you(the LLM) has
                 replied to the text.
                Use this information to recall things from the conversation, or in other words if you need any 
                information from past responses and respond directly to the last message.
                If you do not need the previous information, simply respond with your built-in knowledge. Be very 
                polite and answer the asked questions without deviation. Keep the responses brief without 
                explicitly mentioning what the conversation before was and respond to the answer.
                """);
    }
    public Map<String, List<String>> getConversations() {
        return new HashMap<>(conversations);
    }

    public List<String> getConversationById(String conversationId) {
        return conversations.get(conversationId);
    }

}

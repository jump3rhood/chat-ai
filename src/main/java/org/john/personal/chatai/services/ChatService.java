package org.john.personal.chatai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final ChatClient chatClient;

    // in-memory conversation for manual context storage
    private final Map<String, List<String>> conversations = new HashMap<String, List<String>>();
    private final MessageWindowChatMemory messageWindowChatMemory;

    /*
    * spring autoconfigures the OllamaChatModel from properties.
    * For adding chat history to our chatClient, we need to add a ChatMemory impl (MessageWindowChatMemory)
    * */

    public ChatService(OllamaChatModel ollamaChatModel, MessageWindowChatMemory getChatMemory) {
        // get bean for ChatMemory to get the conversation-history by the conversation Id
        this.messageWindowChatMemory = getChatMemory;
        this.chatClient = ChatClient
                .builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(getChatMemory).build())
                .build();
    }


    /*
    * Chat with context
    * */
    public String chatWithManualContext(String userMessage, String conversationId){

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

    public String chatWithContextMessageMemory(String userMessage, String conversationId){
        return chatClient.prompt(getSimpleChatContextPrompt())
                .user(userMessage)
                .advisors(memory -> memory.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();
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
                Use this information to recall things from the conversation, or in other words if you need any 
                information from past responses and respond directly to the last message.
                If you do not need the previous information, simply respond with your built-in knowledge.Keep the responses brief without 
                explicitly mentioning what the conversation before was and respond to the answer.
                """);
    }
    public Map<String, List<String>> getConversations() {
        return new HashMap<>(conversations);
    }

    public List<Message> getConversationById(String conversationId) {
        return messageWindowChatMemory.get(conversationId);
    }

}

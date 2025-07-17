package org.john.personal.chatai;

import org.john.personal.chatai.services.ChatService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class ChatAiApplication {


    public static void main(String[] args) {


        ConfigurableApplicationContext context = SpringApplication.run(ChatAiApplication.class, args);

        ChatService chatService = context.getBean(ChatService.class);
//        testChat(chatService);
    }

    private static void testChat(ChatService chatService) {
        System.out.println("Sending request 1");
        String conversationId = "test-conversation";

        String response1 = chatService.chatWithContextMessageMemory("My name is John and I love programming", conversationId);
        System.out.println("Response 1: " + response1);

        String response2 = chatService.chatWithContextMessageMemory("What do I like?", conversationId);
        System.out.println("Response 2: " + response2);

        List<Message> history = chatService.getConversationById(conversationId);
        System.out.println("History: " + history);
        }

}

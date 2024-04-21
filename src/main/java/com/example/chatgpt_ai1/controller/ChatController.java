package com.example.chatgpt_ai1.controller;

import com.example.chatgpt_ai1.service.ChatGptService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    static String SYSTEM_MESSAGE = "You are gonna be the 1st actor in a chatroom, given a specific personality and/or topic. You can start the conversation with a message.";

    static int interactionCounter = 1000; // Set the initial value of the counter
    @Getter
    private String ai1Personality;
    @Getter
    private String ai2Personality;

    @Autowired
    private ChatGptService chatGptService;

    @PostMapping("/response")
    public String postChatResponse(@RequestBody String prompt) {
        return chatGptService.getChatResponse(prompt, SYSTEM_MESSAGE);
    }

    @PostMapping("/system-message")
    public ResponseEntity<String> updateSystemMessage(@RequestBody String newSystemMessage) {
        SYSTEM_MESSAGE = newSystemMessage;
        return ResponseEntity.ok("System message updated successfully");
    }


    @PostMapping("/set-personalities")
    public ResponseEntity<String> setPersonalities(@RequestBody PersonalitiesRequest request) {
        ai1Personality = request.getAi1Personality();
        ai2Personality = request.getAi2Personality();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest request) {
        if (interactionCounter <= 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Interaction limit reached.");
        }
        interactionCounter--; // Decrement the counter after each interaction
        String message = request.getMessage();
        String response = generateResponse(message);
        return ResponseEntity.ok(response);
    }

    private String generateResponse(String message) {
        // Use AI personalities to generate response using the ChatGptService
        // Replace the echo response with the actual AI-generated response
        return chatGptService.getChatResponse(message, SYSTEM_MESSAGE);
    }


    // Getter methods for AI personalities
    // You can use these to retrieve personalities when generating responses

    // Request DTOs
    static class PersonalitiesRequest {
        private String ai1Personality;
        private String ai2Personality;

        public String getAi1Personality() {
            return ai1Personality;
        }

        public void setAi1Personality(String ai1Personality) {
            this.ai1Personality = ai1Personality;
        }

        public String getAi2Personality() {
            return ai2Personality;
        }

        public void setAi2Personality(String ai2Personality) {
            this.ai2Personality = ai2Personality;
        }
    }

    static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}

package com.example.chatgpt_ai1.service;

import com.example.chatgpt_ai1.dtos.ChatCompletionRequest;
import com.example.chatgpt_ai1.dtos.ChatCompletionRequest.Message;
import com.example.chatgpt_ai1.dtos.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
public class ChatGptService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGptService.class);

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;



    public String getChatResponse(String userPrompt, String _systemMessage) {
        try {
            WebClient client = WebClient.create();
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel("gpt-3.5-turbo");
            request.setTemperature(0.7);
            request.setMax_tokens(150);
            request.setTop_p(1.0);
            request.setFrequency_penalty(0.0);
            request.setPresence_penalty(0.0);
            request.setMessages(Arrays.asList(
                    new Message("system",  _systemMessage),
                    new Message("user", userPrompt)
            ));

            ChatCompletionResponse response = client.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from ChatGPT API");
            }
        } catch (WebClientResponseException e) {
            logger.error("Error response status code: " + e.getRawStatusCode());
            logger.error("Error response body: " + e.getResponseBodyAsString());
            logger.error("WebClientResponseException", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from ChatGPT API");
        } catch (Exception e) {
            logger.error("Exception", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from ChatGPT API");
        }
    }
}

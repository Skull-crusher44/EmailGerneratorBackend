
/*
* Method	                Description
.get(), .post()         	HTTP methods
.uri()	Set                 target URI
.bodyValue()	            Set request body directly
.retrieve()	                Initiates the request
.bodyToMono()	            Convert to a single object
.bodyToFlux()	            Convert to a stream of objects*/
package com.Email.Email.Response.Generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final WebClient webClient;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailService(EmailType emailRequest) {
        String prompt = buildPrompt(emailRequest);
        String model = emailRequest.getModel();

        if ("openai".equalsIgnoreCase(model)) {
            String response = generateWithOpenAI(prompt);
            return response;
        } else if ("gemini".equalsIgnoreCase(model)) {
            return generateWithGemini(prompt);
        } else {
            return "Unsupported model: " + model;
        }
    }

    private String generateWithGemini(String prompt) {
        Map<String, Object> requestBody = Map.of("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", prompt)
                })
        });

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractGeminiResponse(response);
    }

    private String generateWithOpenAI(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "Give response according to tone."),
                        Map.of("role", "user", "content", prompt)
                }
        );

        String response = webClient.post()
                .uri(openaiApiUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openaiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractOpenAIResponse(response);
    }

    private String extractGeminiResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Gemini parsing error: " + e.getMessage();
        }
    }

    private String extractOpenAIResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            return "OpenAI parsing error: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailType emailRequest) {
        if (emailRequest == null || emailRequest.getEmail() == null || emailRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email content cannot be empty");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Task: Generate a professional email response\n");
        prompt.append("Requirements:\n");
        prompt.append("1. Do not include a subject line\n");
        prompt.append("2. Keep the response professional and concise\n");

        if (emailRequest.getTone() != null && !emailRequest.getTone().trim().isEmpty()) {
            prompt.append("3. Use a ").append(emailRequest.getTone().trim()).append(" tone\n");
        }

        prompt.append("\nOriginal Email:\n");
        prompt.append("-------------------\n");
        prompt.append(emailRequest.getEmail().trim());
        prompt.append("\n-------------------\n");
        prompt.append("\nPlease generate an appropriate response to the above email.");

        return prompt.toString();
    }
}

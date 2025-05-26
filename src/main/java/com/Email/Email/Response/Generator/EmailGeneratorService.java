package com.Email.Email.Response.Generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/*
* Method	                Description
.get(), .post()         	HTTP methods
.uri()	Set                 target URI
.bodyValue()	            Set request body directly
.retrieve()	                Initiates the request
.bodyToMono()	            Convert to a single object
.bodyToFlux()	            Convert to a stream of objects*/

@Service
public class EmailGeneratorService<webClientBuilder> {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;


    private final WebClient webClient;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailService(EmailType emailRequest){
        //Build prompt
         String prompt = buildPrompt(emailRequest);
         //Craft Response
        Map<String,Object> requestbody = Map.of("contents", new Object []{
                        Map.of("parts",new Object[]{
                                        Map.of("text",prompt)
                                }
                        )
                }
        );
        // do req and res
        String response = webClient
                .post()
                .uri(geminiApiUrl+geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestbody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //return res
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            // object mapper is used to parse json object it convert json object to java objects
            ObjectMapper mapper = new ObjectMapper();
            // it converts json data to tree like structure using readTree
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }catch (Exception exception){
            return "error processing request: " + exception.getMessage();
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

        // Add tone specification if provided
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
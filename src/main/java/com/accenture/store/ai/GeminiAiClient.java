//member 4
package com.accenture.store.ai;

import com.accenture.store.exceptions.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the Google Gemini "generateContent" REST endpoint with Spring's
 * RestClient. No AI SDK is added: the request and the answer are plain JSON
 * maps, which keeps the integration small and easy to read.
 *
 * The API key is read from configuration (AI_API_KEY in .env), sent only in the
 * x-goog-api-key request header, and never returned to the React client.
 */
@Component
public class GeminiAiClient implements AiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final int thinkingBudget;

    public GeminiAiClient(@Value("${app.ai.base-url}") String baseUrl,
                          @Value("${app.ai.api-key:}") String apiKey,
                          @Value("${app.ai.model}") String model,
                          @Value("${app.ai.timeout-seconds:30}") int timeoutSeconds,
                          @Value("${app.ai.max-output-tokens:400}") int maxOutputTokens,
                          @Value("${app.ai.thinking-budget:-1}") int thinkingBudget) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.apiKey = apiKey;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.thinkingBudget = thinkingBudget;
    }

    @Override
    public String generate(String systemInstruction, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiUnavailableException(
                    "The AI assistant is not configured. Set AI_API_KEY in .env and restart the application.");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        request.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))));
        request.put("generationConfig", generationConfig());

        Map<?, ?> response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException exception) {
            // Wrong key, retired model, quota or a region block all land here.
            log.warn("AI provider rejected the request: status={} body={}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw new AiUnavailableException("The AI assistant is unavailable right now. Please try again later.");
        } catch (RestClientException exception) {
            log.warn("AI provider call failed: {}", exception.getMessage());
            throw new AiUnavailableException("The AI assistant could not be reached. Please try again later.");
        }

        String answer = extractText(response);
        if (answer.isBlank()) {
            log.warn("AI provider returned no text: {}", response);
            throw new AiUnavailableException("The AI assistant did not return an answer. Please try again.");
        }
        return answer;
    }

    private Map<String, Object> generationConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("temperature", 0.2);
        config.put("maxOutputTokens", maxOutputTokens);

        // Thinking models answer faster and cheaper with thinking switched off, but
        // not every model accepts the field, so it is only sent when configured.
        if (thinkingBudget >= 0) {
            config.put("thinkingConfig", Map.of("thinkingBudget", thinkingBudget));
        }

        return config;
    }

    /** Pulls the answer text out of {@code candidates[0].content.parts[*].text}. */
    private String extractText(Map<?, ?> response) {
        if (response == null || !(response.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()) {
            return "";
        }
        if (!(candidates.get(0) instanceof Map<?, ?> candidate)) {
            return "";
        }
        if (!(candidate.get("content") instanceof Map<?, ?> content)
                || !(content.get("parts") instanceof List<?> parts)) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        for (Object part : parts) {
            if (!(part instanceof Map<?, ?> partMap)) {
                continue;
            }
            // Models with thinking enabled emit "thought" parts that are not the answer.
            if (Boolean.TRUE.equals(partMap.get("thought"))) {
                continue;
            }
            if (partMap.get("text") instanceof String partText && !partText.isBlank()) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(partText.trim());
            }
        }
        return text.toString().trim();
    }
}

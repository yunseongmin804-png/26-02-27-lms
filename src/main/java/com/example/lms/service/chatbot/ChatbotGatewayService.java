package com.example.lms.service.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotGatewayService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${chatbot.gateway.url:http://127.0.0.1:18789/v1/chat/completions}")
    private String gatewayUrl;

    @Value("${chatbot.gateway.token:}")
    private String gatewayToken;

    @Value("${chatbot.gateway.model:openai-codex/gpt-5.3-codex}")
    private String model;

    @Value("${chatbot.system-prompt:당신은 LMS 도우미입니다. 답변은 한국어로 짧고 정확하게 제공하세요.}")
    private String systemPrompt;

    public ChatbotGatewayService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public String ask(String userMessage) {
        if (gatewayToken == null || gatewayToken.isBlank()) {
            throw new IllegalStateException("chatbot.gateway.token 설정이 필요합니다.");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                )
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gatewayUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + gatewayToken)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("챗봇 응답 오류: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            String answer = contentNode.asText("").trim();
            if (answer.isEmpty()) {
                throw new IllegalStateException("챗봇 응답 본문이 비어 있습니다.");
            }
            return answer;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("챗봇 요청 중 인터럽트가 발생했습니다.", e);
        } catch (IOException e) {
            throw new IllegalStateException("챗봇 요청 중 오류가 발생했습니다.", e);
        }
    }
}

package com.example.lms.api;

import com.example.lms.api.dto.ApiResponse;
import com.example.lms.api.dto.ChatbotMessageRequest;
import com.example.lms.api.dto.ChatbotMessageResponse;
import com.example.lms.service.chatbot.ChatbotGatewayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    private final ChatbotGatewayService chatbotGatewayService;

    public ChatbotApiController(ChatbotGatewayService chatbotGatewayService) {
        this.chatbotGatewayService = chatbotGatewayService;
    }

    @PostMapping("/message")
    public ApiResponse<ChatbotMessageResponse> message(@Valid @RequestBody ChatbotMessageRequest request) {
        String answer = chatbotGatewayService.ask(request.message());
        return ApiResponse.ok(new ChatbotMessageResponse(answer));
    }
}

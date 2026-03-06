package com.example.lms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotMessageRequest(
        @NotBlank(message = "message는 비어 있을 수 없습니다")
        @Size(max = 500, message = "message는 500자 이하여야 합니다")
        String message
) {
}

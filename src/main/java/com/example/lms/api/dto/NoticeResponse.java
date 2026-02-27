package com.example.lms.api.dto;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String content,
        String authorName,
        LocalDateTime createdAt
) {}

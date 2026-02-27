package com.example.lms.repository.qna;

import java.time.LocalDateTime;

public record QnaView(
        Long questionId,
        String studentName,
        String title,
        String questionContent,
        LocalDateTime questionCreatedAt,
        Long answerId,
        String responderName,
        String answerContent,
        LocalDateTime answerCreatedAt
) {}

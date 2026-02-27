package com.example.lms.api.dto;

public record CourseResponse(
        Long id,
        String title,
        String category,
        String instructorName,
        Integer capacity
) {}

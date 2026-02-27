package com.example.lms.api.dto;

public record ApiError(
        String code,
        String message
) {}

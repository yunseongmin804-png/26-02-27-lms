package com.example.lms.api.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        int totalCount,
        int totalPages
) {}

package com.example.lms.api.dto;

public record AttendanceSummaryResponse(
        int presentCount,
        int totalCount,
        double attendanceRate
) {}

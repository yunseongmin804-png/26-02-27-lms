package com.example.lms.api.dto;

public record AdminStatsResponse(
        int userCount,
        int courseCount,
        int enrollmentCount,
        int unansweredQnaCount,
        int ungradedSubmissionCount
) {}

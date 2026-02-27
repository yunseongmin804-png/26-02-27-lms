package com.example.lms.api.dto;

import java.time.LocalDate;

public record AttendanceResponse(
        Long attendanceId,
        Long enrollmentId,
        Long userId,
        String userName,
        String courseTitle,
        LocalDate attendanceDate,
        String status
) {}

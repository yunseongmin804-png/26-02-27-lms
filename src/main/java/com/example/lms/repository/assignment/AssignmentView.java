package com.example.lms.repository.assignment;

import java.time.LocalDate;

public record AssignmentView(
        Long assignmentId,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        LocalDate dueDate
) {}

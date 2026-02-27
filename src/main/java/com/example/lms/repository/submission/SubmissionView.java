package com.example.lms.repository.submission;

import java.time.LocalDate;

public record SubmissionView(
        Long submissionId,
        Long assignmentId,
        String assignmentTitle,
        Long studentId,
        String studentName,
        String content,
        String attachmentPath,
        Integer score,
        String feedback,
        LocalDate dueDate
) {}

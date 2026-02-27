package com.example.lms.service;

import com.example.lms.auth.LoginUser;
import com.example.lms.repository.submission.SubmissionJdbcRepository;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.submission.SubmissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTests {

    @Mock
    SubmissionJdbcRepository submissionJdbcRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    SubmissionService submissionService;

    @Test
    void grade_fails_for_non_manager() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");

        assertThrows(IllegalStateException.class,
                () -> submissionService.grade(student, 10L, 90, "ok"));
    }

    @Test
    void grade_fails_for_invalid_score() {
        LoginUser admin = new LoginUser(999L, "관리자", "ADMIN");

        assertThrows(IllegalArgumentException.class,
                () -> submissionService.grade(admin, 10L, 101, "too high"));
    }
}

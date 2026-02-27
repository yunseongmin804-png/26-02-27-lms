package com.example.lms.service;

import com.example.lms.repository.assignment.AssignmentJdbcRepository;
import com.example.lms.repository.assignment.AssignmentView;
import com.example.lms.service.assignment.AssignmentService;
import com.example.lms.service.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTests {

    @Mock
    AssignmentJdbcRepository assignmentJdbcRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    AssignmentService assignmentService;

    @Test
    void submission_open_until_due_date() {
        AssignmentView view = new AssignmentView(2001L, 101L, "강의", "과제", "설명", LocalDate.of(2026, 3, 5));
        when(assignmentJdbcRepository.findViewById(2001L)).thenReturn(Optional.of(view));

        assertTrue(assignmentService.isSubmissionOpen(2001L, LocalDate.of(2026, 3, 5)));
        assertFalse(assignmentService.isSubmissionOpen(2001L, LocalDate.of(2026, 3, 6)));
    }
}

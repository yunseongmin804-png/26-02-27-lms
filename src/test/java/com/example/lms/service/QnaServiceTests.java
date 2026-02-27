package com.example.lms.service;

import com.example.lms.auth.LoginUser;
import com.example.lms.repository.qna.QnaJdbcRepository;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.qna.QnaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class QnaServiceTests {

    @Mock
    QnaJdbcRepository qnaJdbcRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    QnaService qnaService;

    @Test
    void answer_requires_manager_role() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");

        assertThrows(IllegalStateException.class,
                () -> qnaService.answer(student, 5001L, "답변"));
    }

    @Test
    void ask_requires_non_blank_title() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");

        assertThrows(IllegalArgumentException.class,
                () -> qnaService.ask(student, "", "내용"));
    }
}

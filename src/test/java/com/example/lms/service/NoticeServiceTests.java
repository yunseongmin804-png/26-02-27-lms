package com.example.lms.service;

import com.example.lms.auth.LoginUser;
import com.example.lms.repository.notice.NoticeJdbcRepository;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.notice.NoticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTests {

    @Mock
    NoticeJdbcRepository noticeJdbcRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    NoticeService noticeService;

    @Test
    void create_notice_requires_manager_role() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");

        assertThrows(IllegalStateException.class,
                () -> noticeService.createNotice(student, "제목", "내용"));
    }
}

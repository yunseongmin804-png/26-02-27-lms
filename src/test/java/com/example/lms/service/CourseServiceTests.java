package com.example.lms.service;

import com.example.lms.auth.LoginUser;
import com.example.lms.repository.course.CourseJdbcRepository;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.course.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CourseServiceTests {

    @Mock
    CourseJdbcRepository courseJdbcRepository;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    CourseService courseService;

    @Test
    void create_course_requires_manager_role() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");

        assertThrows(IllegalStateException.class,
                () -> courseService.createCourse(student, "스프링", "IT", "강사", 30));
    }
}

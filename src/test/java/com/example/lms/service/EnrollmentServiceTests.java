package com.example.lms.service;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.course.Course;
import com.example.lms.repository.enrollment.EnrollmentJdbcRepository;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTests {

    @Mock
    EnrollmentJdbcRepository enrollmentJdbcRepository;

    @Mock
    CourseService courseService;

    @Mock
    AuditLogService auditLogService;

    @InjectMocks
    EnrollmentService enrollmentService;

    @Test
    void enroll_fails_when_capacity_full() {
        LoginUser student = new LoginUser(1L, "학생1", "STUDENT");
        when(courseService.getCourse(101L)).thenReturn(Optional.of(new Course(101L, "스프링", "IT", "강사", 1)));
        when(enrollmentJdbcRepository.countByCourseId(101L)).thenReturn(1);

        assertThrows(IllegalStateException.class,
                () -> enrollmentService.enroll(student, null, 101L));
    }
}

package com.example.lms.service.enrollment;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.course.Course;
import com.example.lms.domain.enrollment.Enrollment;
import com.example.lms.repository.enrollment.EnrollmentJdbcRepository;
import com.example.lms.repository.enrollment.EnrollmentView;
import com.example.lms.service.course.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.lms.common.security.AccessControl.requireLogin;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentJdbcRepository enrollmentJdbcRepository;
    private final CourseService courseService;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public EnrollmentService(EnrollmentJdbcRepository enrollmentJdbcRepository,
                             CourseService courseService,
                             com.example.lms.service.audit.AuditLogService auditLogService) {
        this.enrollmentJdbcRepository = enrollmentJdbcRepository;
        this.courseService = courseService;
        this.auditLogService = auditLogService;
    }

    public List<EnrollmentView> getAllEnrollments() {
        return enrollmentJdbcRepository.findAllViews();
    }

    public List<EnrollmentView> getEnrollmentsByUser(Long userId) {
        return enrollmentJdbcRepository.findViewsByUserId(userId);
    }

    @Transactional
    public void enroll(LoginUser actor, Long userId, Long courseId) {
        requireLogin(actor);
        if (courseId == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");

        Long requestUserId = actor.isAdmin() ? userId : actor.id();
        if (requestUserId == null) throw new IllegalArgumentException("신청자 정보가 올바르지 않습니다.");

        Course course = courseService.getCourse(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        int currentCount = enrollmentJdbcRepository.countByCourseId(courseId);
        if (currentCount >= course.getCapacity()) {
            throw new IllegalStateException("정원이 가득 찬 강의입니다.");
        }

        Long id = enrollmentJdbcRepository.nextId();
        enrollmentJdbcRepository.save(new Enrollment(id, requestUserId, courseId));
        auditLogService.log(actor, "ENROLLMENT_CREATE", "ENROLLMENT", String.valueOf(id), "courseId=" + courseId + ",userId=" + requestUserId);
    }

    @Transactional
    public void cancelEnrollment(LoginUser actor, Long enrollmentId) {
        requireLogin(actor);
        if (enrollmentId == null) throw new IllegalArgumentException("신청 ID가 필요합니다.");

        if (actor.isAdmin()) {
            enrollmentJdbcRepository.deleteById(enrollmentId);
            auditLogService.log(actor, "ENROLLMENT_CANCEL", "ENROLLMENT", String.valueOf(enrollmentId), "admin cancel");
            return;
        }

        boolean deleted = enrollmentJdbcRepository.deleteByIdAndUserId(enrollmentId, actor.id()) > 0;
        if (!deleted) {
            throw new IllegalStateException("본인 신청건만 취소할 수 있습니다.");
        }
        auditLogService.log(actor, "ENROLLMENT_CANCEL", "ENROLLMENT", String.valueOf(enrollmentId), "self cancel");
    }

    public List<Long> getEnrollmentIdsByCourse(Long courseId) {
        if (courseId == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");
        return enrollmentJdbcRepository.findEnrollmentIdsByCourseId(courseId);
    }

    public List<EnrollmentView> getEnrollmentsByCourse(Long courseId) {
        if (courseId == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");
        return enrollmentJdbcRepository.findViewsByCourseId(courseId);
    }

    public int countEnrollments() {
        return enrollmentJdbcRepository.count();
    }
}

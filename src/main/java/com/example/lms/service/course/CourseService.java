package com.example.lms.service.course;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.course.Course;
import com.example.lms.repository.course.CourseJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.*;

@Service
@Transactional(readOnly = true)
public class CourseService {

    public record CoursePage(List<Course> items, int page, int size, int totalCount, int totalPages) {}

    private final CourseJdbcRepository courseJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public CourseService(CourseJdbcRepository courseJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.courseJdbcRepository = courseJdbcRepository;
        this.auditLogService = auditLogService;
    }

    public List<Course> getCourses() {
        return courseJdbcRepository.findAll();
    }

    public CoursePage searchCourses(String keyword, String category, int page, int size) {
        int validPage = Math.max(page, 1);
        int validSize = Math.max(size, 1);
        int totalCount = courseJdbcRepository.countByFilter(keyword, category);
        int totalPages = Math.max((int) Math.ceil(totalCount / (double) validSize), 1);
        int safePage = Math.min(validPage, totalPages);

        int offset = (safePage - 1) * validSize;
        List<Course> items = courseJdbcRepository.searchPaged(keyword, category, validSize, offset);

        return new CoursePage(items, safePage, validSize, totalCount, totalPages);
    }

    public Optional<Course> getCourse(Long courseId) {
        return courseJdbcRepository.findById(courseId);
    }

    @Transactional
    public void createCourse(LoginUser actor, String title, String category, String instructorName, Integer capacity) {
        requireManager(actor);

        String t = requiredText(title, "강의명", 200);
        String c = requiredText(category, "카테고리", 100);
        String i = requiredText(instructorName, "강사명", 100);
        int cap = positiveNumber(capacity, "정원");

        Long id = courseJdbcRepository.nextId();
        courseJdbcRepository.save(new Course(id, t, c, i, cap));
        auditLogService.log(actor, "COURSE_CREATE", "COURSE", String.valueOf(id), t);
    }

    @Transactional
    public void updateCourse(LoginUser actor, Long id, String title, String category, String instructorName, Integer capacity) {
        requireManager(actor);
        if (id == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");

        String t = requiredText(title, "강의명", 200);
        String c = requiredText(category, "카테고리", 100);
        String i = requiredText(instructorName, "강사명", 100);
        int cap = positiveNumber(capacity, "정원");

        courseJdbcRepository.update(new Course(id, t, c, i, cap));
        auditLogService.log(actor, "COURSE_UPDATE", "COURSE", String.valueOf(id), t);
    }

    @Transactional
    public void deleteCourse(LoginUser actor, Long id) {
        requireManager(actor);
        if (id == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");
        courseJdbcRepository.deleteById(id);
        auditLogService.log(actor, "COURSE_DELETE", "COURSE", String.valueOf(id), null);
    }

    @Transactional
    public int createBulkCourses(LoginUser actor, int count, String titlePrefix, String instructorName, String category, Integer capacity) {
        requireManager(actor);
        int safeCount = Math.max(1, Math.min(count, 100));
        String p = requiredText(titlePrefix, "강의명 접두사", 100);
        String i = requiredText(instructorName, "강사명", 100);
        String c = requiredText(category, "카테고리", 100);
        int cap = positiveNumber(capacity, "정원");

        int created = 0;
        for (int n = 1; n <= safeCount; n++) {
            Long id = courseJdbcRepository.nextId();
            courseJdbcRepository.save(new Course(id, p + " " + n, c, i, cap));
            created++;
        }
        auditLogService.log(actor, "COURSE_BULK_CREATE", "COURSE", null, "count=" + created + ",prefix=" + p);
        return created;
    }

    public int countCourses() {
        return courseJdbcRepository.count();
    }
}

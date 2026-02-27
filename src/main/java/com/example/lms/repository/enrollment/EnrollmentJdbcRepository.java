package com.example.lms.repository.enrollment;

import com.example.lms.domain.enrollment.Enrollment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EnrollmentViewRowMapper viewRowMapper = new EnrollmentViewRowMapper();

    public EnrollmentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments(id, user_id, course_id) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, enrollment.getId(), enrollment.getUserId(), enrollment.getCourseId());
    }

    public int deleteById(Long enrollmentId) {
        return jdbcTemplate.update("DELETE FROM enrollments WHERE id = ?", enrollmentId);
    }

    public int deleteByIdAndUserId(Long enrollmentId, Long userId) {
        return jdbcTemplate.update("DELETE FROM enrollments WHERE id = ? AND user_id = ?", enrollmentId, userId);
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM enrollments", Long.class);
    }

    public List<EnrollmentView> findAllViews() {
        String sql = """
                SELECT
                    e.id AS enrollment_id,
                    u.name AS user_name,
                    u.email AS user_email,
                    c.title AS course_title,
                    c.category AS category
                FROM enrollments e
                JOIN users u ON e.user_id = u.id
                JOIN courses c ON e.course_id = c.id
                ORDER BY e.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper);
    }

    public List<EnrollmentView> findViewsByUserId(Long userId) {
        String sql = """
                SELECT
                    e.id AS enrollment_id,
                    u.name AS user_name,
                    u.email AS user_email,
                    c.title AS course_title,
                    c.category AS category
                FROM enrollments e
                JOIN users u ON e.user_id = u.id
                JOIN courses c ON e.course_id = c.id
                WHERE e.user_id = ?
                ORDER BY e.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, userId);
    }

    public java.util.List<Long> findEnrollmentIdsByCourseId(Long courseId) {
        return jdbcTemplate.queryForList(
                "SELECT id FROM enrollments WHERE course_id = ? ORDER BY id",
                Long.class,
                courseId
        );
    }

    public List<EnrollmentView> findViewsByCourseId(Long courseId) {
        String sql = """
                SELECT
                    e.id AS enrollment_id,
                    u.name AS user_name,
                    u.email AS user_email,
                    c.title AS course_title,
                    c.category AS category
                FROM enrollments e
                JOIN users u ON e.user_id = u.id
                JOIN courses c ON e.course_id = c.id
                WHERE e.course_id = ?
                ORDER BY u.name ASC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, courseId);
    }

    public int countByCourseId(Long courseId) {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enrollments WHERE course_id = ?", Integer.class, courseId);
        return cnt == null ? 0 : cnt;
    }

    public int count() {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM enrollments", Integer.class);
        return cnt == null ? 0 : cnt;
    }
}

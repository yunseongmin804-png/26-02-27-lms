package com.example.lms.repository.assignment;

import com.example.lms.domain.assignment.Assignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AssignmentJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AssignmentViewRowMapper viewRowMapper = new AssignmentViewRowMapper();

    public AssignmentJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AssignmentView> findAllViews() {
        String sql = """
                SELECT a.id AS assignment_id, a.course_id, c.title AS course_title,
                       a.title, a.description, a.due_date
                FROM assignments a
                JOIN courses c ON a.course_id = c.id
                ORDER BY a.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper);
    }

    public List<AssignmentView> findViewsByCourseId(Long courseId) {
        String sql = """
                SELECT a.id AS assignment_id, a.course_id, c.title AS course_title,
                       a.title, a.description, a.due_date
                FROM assignments a
                JOIN courses c ON a.course_id = c.id
                WHERE a.course_id = ?
                ORDER BY a.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, courseId);
    }

    public Optional<AssignmentView> findViewById(Long assignmentId) {
        String sql = """
                SELECT a.id AS assignment_id, a.course_id, c.title AS course_title,
                       a.title, a.description, a.due_date
                FROM assignments a
                JOIN courses c ON a.course_id = c.id
                WHERE a.id = ?
                """;
        List<AssignmentView> rows = jdbcTemplate.query(sql, viewRowMapper, assignmentId);
        return rows.stream().findFirst();
    }

    public int save(Assignment assignment) {
        String sql = "INSERT INTO assignments(id, course_id, title, description, due_date) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                assignment.getId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate());
    }

    public int countDueInDays(int days) {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM assignments
                WHERE due_date IS NOT NULL
                  AND due_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
                """, Integer.class, days);
        return cnt == null ? 0 : cnt;
    }

    public int countOverdue() {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM assignments
                WHERE due_date IS NOT NULL
                  AND due_date < CURRENT_DATE
                """, Integer.class);
        return cnt == null ? 0 : cnt;
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM assignments", Long.class);
    }
}

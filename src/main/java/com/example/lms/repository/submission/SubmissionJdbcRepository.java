package com.example.lms.repository.submission;

import com.example.lms.domain.submission.Submission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SubmissionViewRowMapper viewRowMapper = new SubmissionViewRowMapper();

    public SubmissionJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Submission submission) {
        String sql = "INSERT INTO submissions(id, assignment_id, student_id, content, attachment_path, score, feedback) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                submission.getId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                submission.getContent(),
                submission.getAttachmentPath(),
                submission.getScore(),
                submission.getFeedback());
    }

    public int updateContentAndAttachment(Long assignmentId, Long studentId, String content, String attachmentPath) {
        String sql = """
                UPDATE submissions
                SET content = ?, attachment_path = ?, submitted_at = CURRENT_TIMESTAMP
                WHERE assignment_id = ? AND student_id = ?
                """;
        return jdbcTemplate.update(sql, content, attachmentPath, assignmentId, studentId);
    }

    public Optional<Long> findIdByAssignmentAndStudent(Long assignmentId, Long studentId) {
        List<Long> rows = jdbcTemplate.query(
                "SELECT id FROM submissions WHERE assignment_id = ? AND student_id = ?",
                (rs, rowNum) -> rs.getLong("id"),
                assignmentId, studentId
        );
        return rows.stream().findFirst();
    }

    public int grade(Long submissionId, Integer score, String feedback) {
        String sql = "UPDATE submissions SET score = ?, feedback = ? WHERE id = ?";
        return jdbcTemplate.update(sql, score, feedback, submissionId);
    }

    public List<SubmissionView> findAllViews() {
        String sql = """
                SELECT s.id AS submission_id,
                       s.assignment_id,
                       a.title AS assignment_title,
                       s.student_id,
                       u.name AS student_name,
                       s.content,
                       s.attachment_path,
                       s.score,
                       s.feedback,
                       a.due_date
                FROM submissions s
                JOIN assignments a ON s.assignment_id = a.id
                JOIN users u ON s.student_id = u.id
                ORDER BY s.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper);
    }

    public List<SubmissionView> findViewsByStudentId(Long studentId) {
        String sql = """
                SELECT s.id AS submission_id,
                       s.assignment_id,
                       a.title AS assignment_title,
                       s.student_id,
                       u.name AS student_name,
                       s.content,
                       s.attachment_path,
                       s.score,
                       s.feedback,
                       a.due_date
                FROM submissions s
                JOIN assignments a ON s.assignment_id = a.id
                JOIN users u ON s.student_id = u.id
                WHERE s.student_id = ?
                ORDER BY s.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, studentId);
    }

    public int countUngraded() {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM submissions WHERE score IS NULL", Integer.class);
        return cnt == null ? 0 : cnt;
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM submissions", Long.class);
    }
}

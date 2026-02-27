package com.example.lms.repository.qna;

import com.example.lms.domain.qna.QnaAnswer;
import com.example.lms.domain.qna.QnaQuestion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class QnaJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final QnaViewRowMapper viewRowMapper = new QnaViewRowMapper();

    public QnaJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<QnaView> searchAllPaged(String keyword, String sort, int limit, int offset) {
        String order = "oldest".equalsIgnoreCase(sort) ? "q.id ASC" : "q.id DESC";
        String sql = """
                SELECT q.id AS question_id,
                       u.name AS student_name,
                       q.title,
                       q.content AS question_content,
                       q.created_at AS question_created_at,
                       a.id AS answer_id,
                       a.responder_name,
                       a.content AS answer_content,
                       a.created_at AS answer_created_at
                FROM qna_questions q
                JOIN users u ON q.student_id = u.id
                LEFT JOIN qna_answers a ON q.id = a.question_id
                WHERE (? IS NULL OR ? = '' OR q.title LIKE CONCAT('%', ?, '%') OR q.content LIKE CONCAT('%', ?, '%'))
                """ + " ORDER BY " + order + ", a.id ASC LIMIT " + limit + " OFFSET " + offset;
        return jdbcTemplate.query(sql, viewRowMapper, keyword, keyword, keyword, keyword);
    }

    public int countAllByKeyword(String keyword) {
        String sql = """
                SELECT COUNT(*)
                FROM qna_questions q
                WHERE (? IS NULL OR ? = '' OR q.title LIKE CONCAT('%', ?, '%') OR q.content LIKE CONCAT('%', ?, '%'))
                """;
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, keyword, keyword, keyword, keyword);
        return cnt == null ? 0 : cnt;
    }

    public List<QnaView> searchByStudentPaged(Long studentId, String keyword, String sort, int limit, int offset) {
        String order = "oldest".equalsIgnoreCase(sort) ? "q.id ASC" : "q.id DESC";
        String sql = """
                SELECT q.id AS question_id,
                       u.name AS student_name,
                       q.title,
                       q.content AS question_content,
                       q.created_at AS question_created_at,
                       a.id AS answer_id,
                       a.responder_name,
                       a.content AS answer_content,
                       a.created_at AS answer_created_at
                FROM qna_questions q
                JOIN users u ON q.student_id = u.id
                LEFT JOIN qna_answers a ON q.id = a.question_id
                WHERE q.student_id = ?
                  AND (? IS NULL OR ? = '' OR q.title LIKE CONCAT('%', ?, '%') OR q.content LIKE CONCAT('%', ?, '%'))
                """ + " ORDER BY " + order + ", a.id ASC LIMIT " + limit + " OFFSET " + offset;
        return jdbcTemplate.query(sql, viewRowMapper, studentId, keyword, keyword, keyword, keyword);
    }

    public int countByStudentAndKeyword(Long studentId, String keyword) {
        String sql = """
                SELECT COUNT(*)
                FROM qna_questions q
                WHERE q.student_id = ?
                  AND (? IS NULL OR ? = '' OR q.title LIKE CONCAT('%', ?, '%') OR q.content LIKE CONCAT('%', ?, '%'))
                """;
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, studentId, keyword, keyword, keyword, keyword);
        return cnt == null ? 0 : cnt;
    }

    public int countUnanswered() {
        String sql = """
                SELECT COUNT(*)
                FROM qna_questions q
                LEFT JOIN qna_answers a ON q.id = a.question_id
                WHERE a.question_id IS NULL
                """;
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class);
        return cnt == null ? 0 : cnt;
    }

    public int saveQuestion(QnaQuestion question) {
        String sql = "INSERT INTO qna_questions(id, student_id, title, content) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, question.getId(), question.getStudentId(), question.getTitle(), question.getContent());
    }

    public int saveAnswer(QnaAnswer answer) {
        String sql = "INSERT INTO qna_answers(id, question_id, responder_name, content) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, answer.getId(), answer.getQuestionId(), answer.getResponderName(), answer.getContent());
    }

    public Long nextQuestionId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM qna_questions", Long.class);
    }

    public Long nextAnswerId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM qna_answers", Long.class);
    }
}

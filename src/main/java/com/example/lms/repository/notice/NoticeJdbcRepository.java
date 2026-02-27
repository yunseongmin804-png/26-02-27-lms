package com.example.lms.repository.notice;

import com.example.lms.domain.notice.Notice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoticeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NoticeRowMapper rowMapper = new NoticeRowMapper();

    public NoticeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Notice> searchPaged(String keyword, String sort, int limit, int offset) {
        String orderBy = "oldest".equalsIgnoreCase(sort) ? "id ASC" : "id DESC";
        String sql = """
                SELECT id, title, content, author_name, created_at
                FROM notices
                WHERE (? IS NULL OR ? = '' OR title LIKE CONCAT('%', ?, '%') OR content LIKE CONCAT('%', ?, '%'))
                """ + " ORDER BY " + orderBy + " LIMIT " + limit + " OFFSET " + offset;
        return jdbcTemplate.query(sql, rowMapper, keyword, keyword, keyword, keyword);
    }

    public int countByKeyword(String keyword) {
        String sql = """
                SELECT COUNT(*)
                FROM notices
                WHERE (? IS NULL OR ? = '' OR title LIKE CONCAT('%', ?, '%') OR content LIKE CONCAT('%', ?, '%'))
                """;
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, keyword, keyword, keyword, keyword);
        return cnt == null ? 0 : cnt;
    }

    public Optional<Notice> findById(Long id) {
        String sql = "SELECT id, title, content, author_name, created_at FROM notices WHERE id = ?";
        List<Notice> rows = jdbcTemplate.query(sql, rowMapper, id);
        return rows.stream().findFirst();
    }

    public int save(Notice notice) {
        String sql = "INSERT INTO notices(id, title, content, author_name) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, notice.getId(), notice.getTitle(), notice.getContent(), notice.getAuthorName());
    }

    public int update(Notice notice) {
        String sql = "UPDATE notices SET title = ?, content = ? WHERE id = ?";
        return jdbcTemplate.update(sql, notice.getTitle(), notice.getContent(), notice.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM notices WHERE id = ?", id);
    }

    public boolean existsTodayByTitle(String title) {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM notices
                WHERE title = ?
                  AND DATE(created_at) = CURRENT_DATE
                """, Integer.class, title);
        return cnt != null && cnt > 0;
    }

    public boolean existsRecentByTitleAndContent(String title, String content, int hours) {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM notices
                WHERE title = ?
                  AND content = ?
                  AND created_at >= (CURRENT_TIMESTAMP - INTERVAL ? HOUR)
                """, Integer.class, title, content, hours);
        return cnt != null && cnt > 0;
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM notices", Long.class);
    }
}

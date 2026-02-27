package com.example.lms.repository.course;

import com.example.lms.domain.course.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CourseJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CourseRowMapper rowMapper = new CourseRowMapper();

    public CourseJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Course> findAll() {
        return jdbcTemplate.query("SELECT id, title, category, instructor_name, capacity FROM courses ORDER BY id DESC", rowMapper);
    }

    public List<Course> searchPaged(String keyword, String category, int limit, int offset) {
        String sql = """
                SELECT id, title, category, instructor_name, capacity
                FROM courses
                WHERE (? IS NULL OR ? = '' OR title LIKE CONCAT('%', ?, '%'))
                  AND (? IS NULL OR ? = '' OR category = ?)
                ORDER BY id DESC
                LIMIT ? OFFSET ?
                """;
        return jdbcTemplate.query(sql, rowMapper,
                keyword, keyword, keyword,
                category, category, category,
                limit, offset);
    }

    public int countByFilter(String keyword, String category) {
        String sql = """
                SELECT COUNT(*)
                FROM courses
                WHERE (? IS NULL OR ? = '' OR title LIKE CONCAT('%', ?, '%'))
                  AND (? IS NULL OR ? = '' OR category = ?)
                """;
        Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class,
                keyword, keyword, keyword,
                category, category, category);
        return cnt == null ? 0 : cnt;
    }

    public Optional<Course> findById(Long id) {
        List<Course> rows = jdbcTemplate.query(
                "SELECT id, title, category, instructor_name, capacity FROM courses WHERE id = ?",
                rowMapper,
                id
        );
        return rows.stream().findFirst();
    }

    public int save(Course course) {
        String sql = "INSERT INTO courses(id, title, category, instructor_name, capacity) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, course.getId(), course.getTitle(), course.getCategory(), course.getInstructorName(), course.getCapacity());
    }

    public int update(Course course) {
        String sql = """
                UPDATE courses
                SET title = ?, category = ?, instructor_name = ?, capacity = ?
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql,
                course.getTitle(),
                course.getCategory(),
                course.getInstructorName(),
                course.getCapacity(),
                course.getId());
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM courses WHERE id = ?", id);
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM courses", Long.class);
    }

    public int count() {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM courses", Integer.class);
        return cnt == null ? 0 : cnt;
    }
}

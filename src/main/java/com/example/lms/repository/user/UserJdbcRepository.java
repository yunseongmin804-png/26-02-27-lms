package com.example.lms.repository.user;

import com.example.lms.domain.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper rowMapper = new UserRowMapper();

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT id, email, name, role FROM users ORDER BY id", rowMapper);
    }

    public Optional<User> findById(Long id) {
        List<User> rows = jdbcTemplate.query("SELECT id, email, name, role FROM users WHERE id = ?", rowMapper, id);
        return rows.stream().findFirst();
    }

    public int save(User user) {
        String sql = "INSERT INTO users(id, email, name, role) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM users", Long.class);
    }

    public int count() {
        Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return cnt == null ? 0 : cnt;
    }
}

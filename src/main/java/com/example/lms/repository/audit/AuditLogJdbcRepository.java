package com.example.lms.repository.audit;

import com.example.lms.domain.audit.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditLogRowMapper rowMapper = new AuditLogRowMapper();

    public AuditLogJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(AuditLog log) {
        String sql = """
                INSERT INTO audit_logs(id, actor_id, actor_name, actor_role, action, target_type, target_id, detail)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(sql,
                log.getId(),
                log.getActorId(),
                log.getActorName(),
                log.getActorRole(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetail());
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM audit_logs", Long.class);
    }

    public List<AuditLog> findRecent(int limit) {
        return jdbcTemplate.query(
                "SELECT id, actor_id, actor_name, actor_role, action, target_type, target_id, detail, created_at FROM audit_logs ORDER BY id DESC LIMIT ?",
                rowMapper,
                limit
        );
    }
}

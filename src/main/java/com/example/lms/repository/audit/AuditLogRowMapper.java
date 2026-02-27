package com.example.lms.repository.audit;

import com.example.lms.domain.audit.AuditLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuditLogRowMapper implements RowMapper<AuditLog> {
    @Override
    public AuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AuditLog(
                rs.getLong("id"),
                rs.getObject("actor_id") == null ? null : rs.getLong("actor_id"),
                rs.getString("actor_name"),
                rs.getString("actor_role"),
                rs.getString("action"),
                rs.getString("target_type"),
                rs.getString("target_id"),
                rs.getString("detail"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}

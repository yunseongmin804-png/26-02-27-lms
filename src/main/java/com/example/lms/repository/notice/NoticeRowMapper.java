package com.example.lms.repository.notice;

import com.example.lms.domain.notice.Notice;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NoticeRowMapper implements RowMapper<Notice> {
    @Override
    public Notice mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Notice(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("author_name"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}

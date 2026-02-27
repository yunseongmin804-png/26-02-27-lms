package com.example.lms.repository.submission;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SubmissionViewRowMapper implements RowMapper<SubmissionView> {
    @Override
    public SubmissionView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SubmissionView(
                rs.getLong("submission_id"),
                rs.getLong("assignment_id"),
                rs.getString("assignment_title"),
                rs.getLong("student_id"),
                rs.getString("student_name"),
                rs.getString("content"),
                rs.getString("attachment_path"),
                rs.getObject("score") == null ? null : rs.getInt("score"),
                rs.getString("feedback"),
                rs.getDate("due_date") == null ? null : rs.getDate("due_date").toLocalDate()
        );
    }
}

package com.example.lms.repository.assignment;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AssignmentViewRowMapper implements RowMapper<AssignmentView> {
    @Override
    public AssignmentView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AssignmentView(
                rs.getLong("assignment_id"),
                rs.getLong("course_id"),
                rs.getString("course_title"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("due_date") == null ? null : rs.getDate("due_date").toLocalDate()
        );
    }
}

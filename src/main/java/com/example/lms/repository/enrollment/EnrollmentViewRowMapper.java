package com.example.lms.repository.enrollment;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EnrollmentViewRowMapper implements RowMapper<EnrollmentView> {
    @Override
    public EnrollmentView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new EnrollmentView(
                rs.getLong("enrollment_id"),
                rs.getString("user_name"),
                rs.getString("user_email"),
                rs.getString("course_title"),
                rs.getString("category")
        );
    }
}

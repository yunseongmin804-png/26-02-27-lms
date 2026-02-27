package com.example.lms.repository.attendance;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AttendanceViewRowMapper implements RowMapper<AttendanceView> {
    @Override
    public AttendanceView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AttendanceView(
                rs.getLong("attendance_id"),
                rs.getLong("enrollment_id"),
                rs.getLong("user_id"),
                rs.getString("user_name"),
                rs.getString("course_title"),
                rs.getDate("attendance_date").toLocalDate(),
                rs.getString("status")
        );
    }
}

package com.example.lms.repository.course;

import com.example.lms.domain.course.Course;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseRowMapper implements RowMapper<Course> {
    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Course(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("instructor_name"),
                rs.getInt("capacity")
        );
    }
}

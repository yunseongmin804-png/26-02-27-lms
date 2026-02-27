package com.example.lms.repository.qna;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QnaViewRowMapper implements RowMapper<QnaView> {
    @Override
    public QnaView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new QnaView(
                rs.getLong("question_id"),
                rs.getString("student_name"),
                rs.getString("title"),
                rs.getString("question_content"),
                rs.getTimestamp("question_created_at").toLocalDateTime(),
                rs.getObject("answer_id") == null ? null : rs.getLong("answer_id"),
                rs.getString("responder_name"),
                rs.getString("answer_content"),
                rs.getTimestamp("answer_created_at") == null ? null : rs.getTimestamp("answer_created_at").toLocalDateTime()
        );
    }
}

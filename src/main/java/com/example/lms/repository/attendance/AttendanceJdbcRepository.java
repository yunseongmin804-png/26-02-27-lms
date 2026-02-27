package com.example.lms.repository.attendance;

import com.example.lms.domain.attendance.Attendance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttendanceJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AttendanceViewRowMapper viewRowMapper = new AttendanceViewRowMapper();

    public AttendanceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long nextId() {
        return jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM attendance_records", Long.class);
    }

    public int save(Attendance attendance) {
        String sql = """
                INSERT INTO attendance_records(id, enrollment_id, attendance_date, status)
                VALUES (?, ?, ?, ?)
                """;
        return jdbcTemplate.update(sql,
                attendance.getId(),
                attendance.getEnrollmentId(),
                attendance.getAttendanceDate(),
                attendance.getStatus());
    }

    public int updateStatus(Long attendanceId, String status) {
        return jdbcTemplate.update("UPDATE attendance_records SET status = ? WHERE id = ?", status, attendanceId);
    }

    public int deleteById(Long attendanceId) {
        return jdbcTemplate.update("DELETE FROM attendance_records WHERE id = ?", attendanceId);
    }

    public boolean existsByEnrollmentAndDate(Long enrollmentId, java.time.LocalDate attendanceDate) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM attendance_records WHERE enrollment_id = ? AND attendance_date = ?",
                Integer.class,
                enrollmentId,
                attendanceDate
        );
        return cnt != null && cnt > 0;
    }

    public java.util.Optional<Long> findIdByEnrollmentAndDate(Long enrollmentId, java.time.LocalDate attendanceDate) {
        java.util.List<Long> rows = jdbcTemplate.queryForList(
                "SELECT id FROM attendance_records WHERE enrollment_id = ? AND attendance_date = ?",
                Long.class,
                enrollmentId,
                attendanceDate
        );
        return rows.stream().findFirst();
    }

    public List<AttendanceView> findAllViews(Long courseId, java.time.LocalDate attendanceDate, boolean absentOnly) {
        String sql = """
                SELECT ar.id AS attendance_id,
                       ar.enrollment_id,
                       u.id AS user_id,
                       u.name AS user_name,
                       c.title AS course_title,
                       ar.attendance_date,
                       ar.status
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                JOIN users u ON u.id = e.user_id
                JOIN courses c ON c.id = e.course_id
                WHERE (? IS NULL OR e.course_id = ?)
                  AND (? IS NULL OR ar.attendance_date = ?)
                  AND (? = FALSE OR ar.status = 'ABSENT')
                ORDER BY ar.attendance_date DESC, ar.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, courseId, courseId, attendanceDate, attendanceDate, absentOnly);
    }

    public List<AttendanceView> findAllViewsByDateRange(Long courseId, java.time.LocalDate startDate, java.time.LocalDate endDate, boolean absentOnly) {
        String sql = """
                SELECT ar.id AS attendance_id,
                       ar.enrollment_id,
                       u.id AS user_id,
                       u.name AS user_name,
                       c.title AS course_title,
                       ar.attendance_date,
                       ar.status
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                JOIN users u ON u.id = e.user_id
                JOIN courses c ON c.id = e.course_id
                WHERE (? IS NULL OR e.course_id = ?)
                  AND (? IS NULL OR ar.attendance_date >= ?)
                  AND (? IS NULL OR ar.attendance_date <= ?)
                  AND (? = FALSE OR ar.status = 'ABSENT')
                ORDER BY ar.attendance_date DESC, ar.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper,
                courseId, courseId,
                startDate, startDate,
                endDate, endDate,
                absentOnly);
    }

    public List<AttendanceView> findViewsByUserId(Long userId) {
        String sql = """
                SELECT ar.id AS attendance_id,
                       ar.enrollment_id,
                       u.id AS user_id,
                       u.name AS user_name,
                       c.title AS course_title,
                       ar.attendance_date,
                       ar.status
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                JOIN users u ON u.id = e.user_id
                JOIN courses c ON c.id = e.course_id
                WHERE u.id = ?
                ORDER BY ar.attendance_date DESC, ar.id DESC
                """;
        return jdbcTemplate.query(sql, viewRowMapper, userId);
    }

    public int countPresentByUserId(Long userId) {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                WHERE e.user_id = ? AND ar.status = 'PRESENT'
                """, Integer.class, userId);
        return cnt == null ? 0 : cnt;
    }

    public int countTotalByUserId(Long userId) {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                WHERE e.user_id = ?
                """, Integer.class, userId);
        return cnt == null ? 0 : cnt;
    }

    public int countPresentByEnrollmentId(Long enrollmentId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM attendance_records WHERE enrollment_id = ? AND status = 'PRESENT'",
                Integer.class,
                enrollmentId
        );
        return cnt == null ? 0 : cnt;
    }

    public int countTotalByEnrollmentId(Long enrollmentId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM attendance_records WHERE enrollment_id = ?",
                Integer.class,
                enrollmentId
        );
        return cnt == null ? 0 : cnt;
    }

    public java.util.List<StudentAttendanceSummaryRow> summarizeByCourse(Long courseId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String sql = """
                SELECT e.id AS enrollment_id,
                       u.name AS student_name,
                       c.title AS course_title,
                       SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
                       COUNT(*) AS total_count
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                JOIN users u ON u.id = e.user_id
                JOIN courses c ON c.id = e.course_id
                WHERE (? IS NULL OR e.course_id = ?)
                  AND (? IS NULL OR ar.attendance_date >= ?)
                  AND (? IS NULL OR ar.attendance_date <= ?)
                GROUP BY e.id, u.name, c.title
                ORDER BY c.title ASC, u.name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StudentAttendanceSummaryRow(
                        rs.getLong("enrollment_id"),
                        rs.getString("student_name"),
                        rs.getString("course_title"),
                        rs.getInt("present_count"),
                        rs.getInt("total_count")
                ),
                courseId, courseId,
                startDate, startDate,
                endDate, endDate);
    }

    public java.util.List<AbsentTopRow> findTopAbsentees(int limit, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String sql = """
                SELECT u.name AS student_name,
                       c.title AS course_title,
                       SUM(CASE WHEN ar.status = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
                       COUNT(*) AS total_count
                FROM attendance_records ar
                JOIN enrollments e ON e.id = ar.enrollment_id
                JOIN users u ON u.id = e.user_id
                JOIN courses c ON c.id = e.course_id
                WHERE (? IS NULL OR ar.attendance_date >= ?)
                  AND (? IS NULL OR ar.attendance_date <= ?)
                GROUP BY u.name, c.title
                HAVING absent_count > 0
                ORDER BY absent_count DESC, total_count DESC, u.name ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new AbsentTopRow(
                        rs.getString("student_name"),
                        rs.getString("course_title"),
                        rs.getInt("absent_count"),
                        rs.getInt("total_count")
                ),
                startDate, startDate,
                endDate, endDate,
                limit);
    }

    public int countTodayPresent() {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM attendance_records
                WHERE attendance_date = CURRENT_DATE AND status = 'PRESENT'
                """, Integer.class);
        return cnt == null ? 0 : cnt;
    }

    public int countTodayTotal() {
        Integer cnt = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM attendance_records
                WHERE attendance_date = CURRENT_DATE
                """, Integer.class);
        return cnt == null ? 0 : cnt;
    }
}

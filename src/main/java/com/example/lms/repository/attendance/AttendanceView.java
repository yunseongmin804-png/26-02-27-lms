package com.example.lms.repository.attendance;

import java.time.LocalDate;

public class AttendanceView {
    private final Long attendanceId;
    private final Long enrollmentId;
    private final Long userId;
    private final String userName;
    private final String courseTitle;
    private final LocalDate attendanceDate;
    private final String status;

    public AttendanceView(Long attendanceId, Long enrollmentId, Long userId, String userName,
                          String courseTitle, LocalDate attendanceDate, String status) {
        this.attendanceId = attendanceId;
        this.enrollmentId = enrollmentId;
        this.userId = userId;
        this.userName = userName;
        this.courseTitle = courseTitle;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public Long getAttendanceId() { return attendanceId; }
    public Long getEnrollmentId() { return enrollmentId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCourseTitle() { return courseTitle; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String getStatus() { return status; }
}

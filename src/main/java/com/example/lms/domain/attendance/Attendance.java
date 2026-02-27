package com.example.lms.domain.attendance;

import java.time.LocalDate;

public class Attendance {
    private Long id;
    private Long enrollmentId;
    private LocalDate attendanceDate;
    private String status;

    public Attendance(Long id, Long enrollmentId, LocalDate attendanceDate, String status) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getEnrollmentId() { return enrollmentId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public String getStatus() { return status; }
}

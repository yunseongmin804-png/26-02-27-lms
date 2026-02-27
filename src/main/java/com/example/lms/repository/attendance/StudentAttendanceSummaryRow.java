package com.example.lms.repository.attendance;

public record StudentAttendanceSummaryRow(
        Long enrollmentId,
        String studentName,
        String courseTitle,
        int presentCount,
        int totalCount
) {
    public double attendanceRate() {
        return totalCount == 0 ? 0d : (presentCount * 100.0 / totalCount);
    }
}

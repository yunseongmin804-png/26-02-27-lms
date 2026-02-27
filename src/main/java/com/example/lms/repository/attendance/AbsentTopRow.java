package com.example.lms.repository.attendance;

public record AbsentTopRow(
        String studentName,
        String courseTitle,
        int absentCount,
        int totalCount
) {
    public double absentRate() {
        return totalCount == 0 ? 0d : (absentCount * 100.0 / totalCount);
    }
}

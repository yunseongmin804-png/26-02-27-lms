package com.example.lms.repository.enrollment;

/**
 * JOIN 조회 결과를 담는 화면용 DTO
 */
public class EnrollmentView {
    private final Long enrollmentId;
    private final String userName;
    private final String userEmail;
    private final String courseTitle;
    private final String category;

    public EnrollmentView(Long enrollmentId, String userName, String userEmail, String courseTitle, String category) {
        this.enrollmentId = enrollmentId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.courseTitle = courseTitle;
        this.category = category;
    }

    public Long getEnrollmentId() { return enrollmentId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getCourseTitle() { return courseTitle; }
    public String getCategory() { return category; }
}

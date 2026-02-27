package com.example.lms.auth;

/**
 * 세션에 저장할 로그인 사용자 정보
 */
public record LoginUser(Long id, String name, String role) {
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isInstructor() { return "INSTRUCTOR".equals(role); }
    public boolean canManageCourses() { return isAdmin() || isInstructor(); }
}

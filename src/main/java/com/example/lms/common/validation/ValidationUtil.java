package com.example.lms.common.validation;

public final class ValidationUtil {
    private ValidationUtil() {}

    public static String requiredText(String value, String fieldName, int maxLen) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLen) {
            throw new IllegalArgumentException(fieldName + "은(는) " + maxLen + "자 이하여야 합니다.");
        }
        return trimmed;
    }

    public static int positiveNumber(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + "은(는) 1 이상이어야 합니다.");
        }
        return value;
    }

    public static int score(Integer score) {
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException("점수는 0~100 사이여야 합니다.");
        }
        return score;
    }

    public static String role(String role) {
        String r = requiredText(role, "역할", 30).toUpperCase();
        if (!("STUDENT".equals(r) || "INSTRUCTOR".equals(r) || "ADMIN".equals(r))) {
            throw new IllegalArgumentException("역할은 STUDENT/INSTRUCTOR/ADMIN 중 하나여야 합니다.");
        }
        return r;
    }

    public static String email(String email) {
        String e = requiredText(email, "이메일", 100);
        if (!e.contains("@")) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다.");
        }
        return e;
    }
}

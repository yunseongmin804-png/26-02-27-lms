package com.example.lms.common.security;

import com.example.lms.auth.LoginUser;

public final class AccessControl {
    private AccessControl() {}

    public static void requireLogin(LoginUser actor) {
        if (actor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
    }

    public static void requireManager(LoginUser actor) {
        requireLogin(actor);
        if (!actor.canManageCourses()) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

    public static void requireAdmin(LoginUser actor) {
        requireLogin(actor);
        if (!actor.isAdmin()) {
            throw new IllegalStateException("관리자 권한이 필요합니다.");
        }
    }
}

package com.example.lms.common.web;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 관리자/강사 전용 URL 보호용 인터셉터
 */
@Component
public class ManagerGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : AuthUtil.loginUser(session);

        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (!loginUser.canManageCourses()) {
            response.sendRedirect("/?message=%EA%B6%8C%ED%95%9C%EC%9D%B4+%EC%97%86%EC%8A%B5%EB%8B%88%EB%8B%A4");
            return false;
        }

        return true;
    }
}

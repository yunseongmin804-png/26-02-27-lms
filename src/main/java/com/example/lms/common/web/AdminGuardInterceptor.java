package com.example.lms.common.web;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminGuardInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : AuthUtil.loginUser(session);

        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (!loginUser.isAdmin()) {
            response.sendRedirect("/?message=%EA%B4%80%EB%A6%AC%EC%9E%90+%EA%B6%8C%ED%95%9C%EC%9D%B4+%ED%95%84%EC%9A%94%ED%95%A9%EB%8B%88%EB%8B%A4");
            return false;
        }

        return true;
    }
}

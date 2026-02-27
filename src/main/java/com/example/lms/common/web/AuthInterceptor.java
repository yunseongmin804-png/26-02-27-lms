package com.example.lms.common.web;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginUser loginUser = session == null ? null : AuthUtil.loginUser(session);

        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}

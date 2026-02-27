package com.example.lms.common;

import com.example.lms.auth.LoginUser;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
    private AuthUtil() {}

    public static LoginUser loginUser(HttpSession session) {
        Object value = session.getAttribute("loginUser");
        if (value instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}

package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.user.User;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginForm(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("error", error);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam Long userId, HttpSession session) {
        User user = userService.getUser(userId).orElse(null);
        if (user == null) {
            return "redirect:/login?error=사용자를+찾을+수+없습니다";
        }

        session.setAttribute("loginUser", new LoginUser(user.getId(), user.getName(), user.getRole()));
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

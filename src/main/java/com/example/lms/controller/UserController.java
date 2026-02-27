package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String users(@RequestParam(required = false) String message, Model model, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("message", message);
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        model.addAttribute("loginUser", loginUser);
        return "users/form";
    }

    @PostMapping
    public String create(@RequestParam String email,
                         @RequestParam String name,
                         @RequestParam(defaultValue = "STUDENT") String role,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        userService.createUser(loginUser, email, name, role);
        return "redirect:/users?message=사용자가+등록되었습니다";
    }

    @PostMapping("/bulk-students")
    public String bulkStudents(@RequestParam(defaultValue = "20") int count,
                               @RequestParam(defaultValue = "학생") String prefix,
                               HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        int created = userService.createBulkStudents(loginUser, count, prefix);
        return "redirect:/users?message=" + created + "+명의+학생이+일괄+생성되었습니다";
    }
}

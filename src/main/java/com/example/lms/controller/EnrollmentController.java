package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;

import static com.example.lms.common.security.AccessControl.requireAdmin;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final CourseService courseService;

    public EnrollmentController(EnrollmentService enrollmentService, UserService userService, CourseService courseService) {
        this.enrollmentService = enrollmentService;
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String message,
                       HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("enrollments", loginUser.isAdmin()
                ? enrollmentService.getAllEnrollments()
                : enrollmentService.getEnrollmentsByUser(loginUser.id()));
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("courses", courseService.getCourses());
        model.addAttribute("message", message);
        return "enrollments/list";
    }

    @PostMapping
    public String enroll(@RequestParam(required = false) Long userId,
                         @RequestParam Long courseId,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        try {
            enrollmentService.enroll(loginUser, userId, courseId);
            return "redirect:/enrollments?message=수강신청이+완료되었습니다";
        } catch (DuplicateKeyException e) {
            return "redirect:/enrollments?message=이미+신청한+강의입니다";
        }
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam Long enrollmentId, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        enrollmentService.cancelEnrollment(loginUser, enrollmentId);
        return "redirect:/enrollments?message=수강신청이+취소되었습니다";
    }

    @PostMapping("/bulk-students")
    public String bulkEnrollStudents(@RequestParam Long courseId,
                                     @RequestParam(defaultValue = "30") int limit,
                                     HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        requireAdmin(loginUser);

        int safeLimit = Math.max(1, Math.min(limit, 300));
        int created = 0;
        int skipped = 0;
        for (var u : userService.getUsers()) {
            if (!"STUDENT".equals(u.getRole())) continue;
            if (created >= safeLimit) break;
            try {
                enrollmentService.enroll(loginUser, u.getId(), courseId);
                created++;
            } catch (Exception e) {
                skipped++;
            }
        }
        return "redirect:/enrollments?message=" + created + "+명+일괄+신청,+" + skipped + "+건+스킵";
    }
}

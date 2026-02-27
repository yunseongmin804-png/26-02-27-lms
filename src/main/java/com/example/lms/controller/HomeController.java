package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.assignment.AssignmentService;
import com.example.lms.service.attendance.AttendanceService;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import com.example.lms.service.qna.QnaService;
import com.example.lms.service.submission.SubmissionService;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final QnaService qnaService;
    private final SubmissionService submissionService;
    private final AttendanceService attendanceService;
    private final AssignmentService assignmentService;

    public HomeController(UserService userService,
                          CourseService courseService,
                          EnrollmentService enrollmentService,
                          QnaService qnaService,
                          SubmissionService submissionService,
                          AttendanceService attendanceService,
                          AssignmentService assignmentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.qnaService = qnaService;
        this.submissionService = submissionService;
        this.attendanceService = attendanceService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("courseCount", courseService.countCourses());
        model.addAttribute("enrollmentCount", enrollmentService.countEnrollments());
        model.addAttribute("unansweredQnaCount", qnaService.countUnanswered());
        model.addAttribute("ungradedSubmissionCount", submissionService.countUngraded());
        model.addAttribute("attendanceSummary", attendanceService.getSummary(loginUser));
        model.addAttribute("dueSoonCount", assignmentService.countDueInDays(7));
        model.addAttribute("overdueCount", assignmentService.countOverdue());
        return "home";
    }
}

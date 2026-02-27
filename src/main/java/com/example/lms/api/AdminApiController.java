package com.example.lms.api;

import com.example.lms.api.dto.AdminStatsResponse;
import com.example.lms.api.dto.ApiResponse;
import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import com.example.lms.service.qna.QnaService;
import com.example.lms.service.submission.SubmissionService;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.lms.common.security.AccessControl.requireAdmin;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final QnaService qnaService;
    private final SubmissionService submissionService;

    public AdminApiController(UserService userService,
                              CourseService courseService,
                              EnrollmentService enrollmentService,
                              QnaService qnaService,
                              SubmissionService submissionService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.qnaService = qnaService;
        this.submissionService = submissionService;
    }

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> stats(HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        requireAdmin(loginUser);

        AdminStatsResponse response = new AdminStatsResponse(
                userService.countUsers(),
                courseService.countCourses(),
                enrollmentService.countEnrollments(),
                qnaService.countUnanswered(),
                submissionService.countUngraded()
        );
        return ApiResponse.ok(response);
    }
}

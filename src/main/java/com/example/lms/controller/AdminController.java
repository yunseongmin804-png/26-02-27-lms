package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.assignment.AssignmentService;
import com.example.lms.service.attendance.AttendanceService;
import com.example.lms.service.audit.AuditLogService;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import com.example.lms.service.notice.NoticeService;
import com.example.lms.service.qna.QnaService;
import com.example.lms.service.submission.SubmissionService;
import com.example.lms.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import static com.example.lms.common.security.AccessControl.requireAdmin;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final QnaService qnaService;
    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final AttendanceService attendanceService;
    private final NoticeService noticeService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService,
                           CourseService courseService,
                           EnrollmentService enrollmentService,
                           QnaService qnaService,
                           SubmissionService submissionService,
                           AssignmentService assignmentService,
                           AttendanceService attendanceService,
                           NoticeService noticeService,
                           AuditLogService auditLogService) {
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.qnaService = qnaService;
        this.submissionService = submissionService;
        this.assignmentService = assignmentService;
        this.attendanceService = attendanceService;
        this.noticeService = noticeService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "30") double threshold,
                            @RequestParam(defaultValue = "10") int riskLimit,
                            @RequestParam(required = false) String message,
                            HttpSession session,
                            Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        double safeThreshold = Math.max(0d, Math.min(threshold, 100d));
        int safeRiskLimit = Math.max(1, Math.min(riskLimit, 100));

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("courseCount", courseService.countCourses());
        model.addAttribute("enrollmentCount", enrollmentService.countEnrollments());
        model.addAttribute("unansweredQnaCount", qnaService.countUnanswered());
        model.addAttribute("ungradedSubmissionCount", submissionService.countUngraded());
        model.addAttribute("riskAbsentThreshold", safeThreshold);
        model.addAttribute("riskLimit", safeRiskLimit);
        model.addAttribute("riskAbsentCount", attendanceService.countRiskAbsentees(loginUser, safeThreshold));
        model.addAttribute("riskAbsentees", attendanceService.getRiskAbsentees(loginUser, safeThreshold, safeRiskLimit));
        model.addAttribute("topAbsentees", attendanceService.getTopAbsentees(loginUser, 10, null, null));
        model.addAttribute("recentAuditLogs", auditLogService.getRecent(20));
        model.addAttribute("message", message);
        return "admin/dashboard";
    }

    @PostMapping("/demo-seed")
    public String demoSeed(HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        requireAdmin(loginUser);

        int students = userService.createBulkStudents(loginUser, 30, "데모학생");
        int courses = courseService.createBulkCourses(loginUser, 6, "데모강의", "강사1", "IT/외국어", 40);

        int enrollCreated = 0;
        int enrollSkipped = 0;
        var allCourses = courseService.getCourses();
        Long targetCourseId = allCourses.isEmpty() ? null : allCourses.get(allCourses.size() - 1).getId();
        if (targetCourseId != null) {
            for (var u : userService.getUsers()) {
                if (!"STUDENT".equals(u.getRole())) continue;
                try {
                    enrollmentService.enroll(loginUser, u.getId(), targetCourseId);
                    enrollCreated++;
                } catch (Exception e) {
                    enrollSkipped++;
                }
            }
            assignmentService.createBulkAssignments(loginUser, targetCourseId, 8, "데모과제", 1);
        }

        // 공지/Q&A 데모 데이터도 같이 생성
        noticeService.createNotice(loginUser, "[공지] LMS 데모 공지", "데모 환경입니다. 기능 시연을 위해 생성된 공지입니다.");
        noticeService.createNotice(loginUser, "[공지] 과제 제출 안내", "과제는 마감일 전까지 제출/재제출 가능합니다.");

        var studentActors = userService.getUsers().stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .limit(2)
                .map(u -> new com.example.lms.auth.LoginUser(u.getId(), u.getName(), u.getRole()))
                .toList();
        for (var s : studentActors) {
            qnaService.ask(s, "데모 질문 - " + s.name(), "출석/과제 확인은 어디서 하나요?");
        }

        String msg = "데모세팅 완료 - 학생 " + students + "명, 강의 " + courses + "개, 수강 " + enrollCreated + "건(" + enrollSkipped + " 스킵), 과제 8개, 공지/Q&A 샘플 생성";
        return "redirect:/admin/dashboard?threshold=30&riskLimit=10&message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
    }

    @GetMapping("/risk-notice-draft")
    public String riskNoticeDraft(@RequestParam(defaultValue = "30") double threshold,
                                  @RequestParam(defaultValue = "10") int riskLimit,
                                  HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        double safeThreshold = Math.max(0d, Math.min(threshold, 100d));
        int safeRiskLimit = Math.max(1, Math.min(riskLimit, 100));

        var targets = attendanceService.getRiskAbsentees(loginUser, safeThreshold, safeRiskLimit);
        String title = "[출석 안내] 결석률 관리 공지 (" + String.format(java.util.Locale.ROOT, "%.1f", safeThreshold) + "% 이상)";

        StringBuilder content = new StringBuilder();
        content.append("안녕하세요. 출석 관리 안내드립니다.\n");
        content.append("현재 결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("% 이상 대상자 기준으로 점검 중입니다.\n\n");
        content.append("- 안내 일자: ").append(LocalDate.now()).append("\n");
        content.append("- 대상 기준: 결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("% 이상\n");
        content.append("- 점검 대상 수: ").append(targets.size()).append("명\n\n");

        if (targets.isEmpty()) {
            content.append("현재 기준에서 대상자가 없습니다.\n");
        } else {
            content.append("[대상 요약]\n");
            for (var r : targets) {
                content.append("- ").append(r.studentName())
                        .append(" / ").append(r.courseTitle())
                        .append(" / 결석 ").append(r.absentCount()).append("회")
                        .append(" (결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate())).append("%)\n");
            }
            content.append("\n출석 개선 상담이 필요한 학생은 담당 강사에게 문의 바랍니다.\n");
        }

        return "redirect:/notices?draftTitle=" + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&draftContent=" + URLEncoder.encode(content.toString(), StandardCharsets.UTF_8)
                + "&message=" + URLEncoder.encode("출석 위험 공지 초안이 채워졌습니다", StandardCharsets.UTF_8);
    }

    @GetMapping("/risk-notice-draft-by-course")
    public ResponseEntity<byte[]> riskNoticeDraftByCourse(@RequestParam(defaultValue = "30") double threshold,
                                                           @RequestParam(defaultValue = "20") int riskLimit,
                                                           HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        double safeThreshold = Math.max(0d, Math.min(threshold, 100d));
        int safeRiskLimit = Math.max(1, Math.min(riskLimit, 100));

        var targets = attendanceService.getRiskAbsentees(loginUser, safeThreshold, safeRiskLimit);
        var grouped = new LinkedHashMap<String, java.util.List<com.example.lms.repository.attendance.AbsentTopRow>>();
        for (var r : targets) {
            grouped.computeIfAbsent(r.courseTitle(), k -> new java.util.ArrayList<>()).add(r);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# 과목별 출석 위험 공지 초안\n");
        sb.append("- 생성일: ").append(LocalDate.now()).append("\n");
        sb.append("- 기준 결석률: ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("%\n\n");

        if (grouped.isEmpty()) {
            sb.append("현재 기준에서 대상 학생이 없습니다.\n");
        } else {
            for (var entry : grouped.entrySet()) {
                String courseTitle = entry.getKey();
                var rows = entry.getValue();

                sb.append("## 공지 제목\n");
                sb.append("[출석 안내] ").append(courseTitle).append(" 결석률 관리 안내\n\n");
                sb.append("## 공지 본문\n");
                sb.append("안녕하세요. ").append(courseTitle).append(" 강의 출석 관리 안내드립니다.\n");
                sb.append("현재 결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("% 이상 학생을 대상으로 점검 중입니다.\n\n");
                sb.append("[대상 요약]\n");
                for (var r : rows) {
                    sb.append("- ").append(r.studentName())
                            .append(" / 결석 ").append(r.absentCount()).append("회")
                            .append(" (결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate())).append("%)\n");
                }
                sb.append("\n출석 개선 상담이 필요한 경우 담당 강사에게 문의 바랍니다.\n");
                sb.append("\n---\n\n");
            }
        }

        String filename = "risk-notice-draft-by-course.md";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/risk-notice-create-by-course")
    public String riskNoticeCreateByCourse(@RequestParam(defaultValue = "30") double threshold,
                                           @RequestParam(defaultValue = "20") int riskLimit,
                                           HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        double safeThreshold = Math.max(0d, Math.min(threshold, 100d));
        int safeRiskLimit = Math.max(1, Math.min(riskLimit, 100));

        var targets = attendanceService.getRiskAbsentees(loginUser, safeThreshold, safeRiskLimit);
        var grouped = new LinkedHashMap<String, java.util.List<com.example.lms.repository.attendance.AbsentTopRow>>();
        for (var r : targets) {
            grouped.computeIfAbsent(r.courseTitle(), k -> new java.util.ArrayList<>()).add(r);
        }

        int created = 0;
        int skipped = 0;
        for (var entry : grouped.entrySet()) {
            String courseTitle = entry.getKey();
            var rows = entry.getValue();

            String title = "[출석 안내] " + courseTitle + " 결석률 관리 안내";
            StringBuilder content = new StringBuilder();
            content.append("안녕하세요. ").append(courseTitle).append(" 강의 출석 관리 안내드립니다.\n");
            content.append("현재 결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("% 이상 학생을 대상으로 점검 중입니다.\n\n");
            content.append("[대상 요약]\n");
            for (var r : rows) {
                content.append("- ").append(r.studentName())
                        .append(" / 결석 ").append(r.absentCount()).append("회")
                        .append(" (결석률 ").append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate())).append("%)\n");
            }
            content.append("\n출석 개선 상담이 필요한 경우 담당 강사에게 문의 바랍니다.\n");

            boolean ok = noticeService.createNoticeIfNotExistsToday(loginUser, title, content.toString());
            if (ok) created++; else skipped++;
        }

        String msg;
        if (created == 0 && skipped == 0) msg = "생성할 과목별 공지 대상이 없습니다";
        else msg = created + "건 생성, " + skipped + "건 중복으로 건너뜀";
        return "redirect:/notices?message=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
    }

    @GetMapping("/risk-message-template")
    public ResponseEntity<byte[]> riskMessageTemplate(@RequestParam(defaultValue = "30") double threshold,
                                                      @RequestParam(defaultValue = "10") int riskLimit,
                                                      HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        double safeThreshold = Math.max(0d, Math.min(threshold, 100d));
        int safeRiskLimit = Math.max(1, Math.min(riskLimit, 100));

        StringBuilder sb = new StringBuilder();
        sb.append("[출석 경고 안내 템플릿]\n");
        sb.append("기준 결석률: ").append(String.format(java.util.Locale.ROOT, "%.1f", safeThreshold)).append("%\n\n");

        var targets = attendanceService.getRiskAbsentees(loginUser, safeThreshold, safeRiskLimit);
        if (targets.isEmpty()) {
            sb.append("현재 기준에서 경고 대상 학생이 없습니다.\n");
        } else {
            for (var r : targets) {
                sb.append("- 학생: ").append(r.studentName())
                        .append(" / 강의: ").append(r.courseTitle())
                        .append(" / 결석률: ").append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate())).append("%\n")
                        .append("  메시지 예시: 안녕하세요 ").append(r.studentName())
                        .append("님, 현재 ").append(r.courseTitle())
                        .append(" 강의 결석률이 ").append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate())).append("%입니다. 출석 개선 상담이 필요합니다.\n\n");
            }
        }

        String filename = "risk-message-template.txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}

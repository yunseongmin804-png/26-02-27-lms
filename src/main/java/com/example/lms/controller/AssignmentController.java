package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.assignment.AssignmentService;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.submission.SubmissionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private static final long MAX_ATTACHMENT_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "gif", "txt", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "zip");
    private static final Map<String, Set<String>> ALLOWED_CONTENT_TYPES_BY_EXT = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint")),
            Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed"))
    );

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final CourseService courseService;

    @Value("${app.upload-dir:/tmp/lms-uploads}")
    private String uploadDir;

    public AssignmentController(AssignmentService assignmentService,
                                SubmissionService submissionService,
                                CourseService courseService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.courseService = courseService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String message,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(defaultValue = "all") String mode,
                       Model model,
                       HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("assignments", assignmentService.getAssignments(courseId));
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("courses", courseService.getCourses());

        boolean manageMode = "manage".equalsIgnoreCase(mode);
        boolean myMode = "my".equalsIgnoreCase(mode);

        model.addAttribute("showCreateForm", manageMode && loginUser.canManageCourses());
        model.addAttribute("showGrading", manageMode && loginUser.canManageCourses());

        if (myMode || !loginUser.canManageCourses()) {
            model.addAttribute("submissions", submissionService.getMySubmissions(loginUser.id()));
        } else {
            model.addAttribute("submissions", submissionService.getAllSubmissions());
        }

        model.addAttribute("mode", mode);
        model.addAttribute("message", message);
        return "assignments/list";
    }

    @GetMapping("/manage")
    public String managePage(HttpSession session) {
        return "redirect:/assignments?mode=manage";
    }

    @GetMapping("/my")
    public String myPage(HttpSession session) {
        return "redirect:/assignments?mode=my";
    }

    @PostMapping
    public String create(@RequestParam Long courseId,
                         @RequestParam String title,
                         @RequestParam String description,
                         @RequestParam(required = false) String dueDate,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        LocalDate parsedDate = (dueDate == null || dueDate.isBlank()) ? null : LocalDate.parse(dueDate);
        assignmentService.createAssignment(loginUser, courseId, title, description, parsedDate);
        return "redirect:/assignments?message=과제가+등록되었습니다";
    }

    @PostMapping("/bulk")
    public String bulkCreate(@RequestParam Long courseId,
                             @RequestParam(defaultValue = "10") int count,
                             @RequestParam(defaultValue = "샘플 과제") String titlePrefix,
                             @RequestParam(defaultValue = "1") Integer dueAfterDays,
                             HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        int created = assignmentService.createBulkAssignments(loginUser, courseId, count, titlePrefix, dueAfterDays);
        return "redirect:/assignments?mode=manage&message=" + created + "+개+과제가+일괄+생성되었습니다";
    }

    @PostMapping("/{assignmentId}/submit")
    public String submit(@PathVariable Long assignmentId,
                         @RequestParam String content,
                         @RequestParam(required = false) MultipartFile attachment,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        if (!assignmentService.isSubmissionOpen(assignmentId, LocalDate.now())) {
            return "redirect:/assignments?message=마감일이+지난+과제는+제출할+수+없습니다";
        }

        String attachmentPath = saveAttachment(attachment);
        submissionService.submitOrResubmit(assignmentId, loginUser.id(), content, attachmentPath);
        return "redirect:/assignments?message=과제+제출(재제출)+완료";
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public String grade(@PathVariable Long submissionId,
                        @RequestParam Integer score,
                        @RequestParam(required = false) String feedback,
                        HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        submissionService.grade(loginUser, submissionId, score, feedback);
        return "redirect:/assignments?message=채점이+완료되었습니다";
    }

    private String saveAttachment(MultipartFile attachment) {
        if (attachment == null || attachment.isEmpty()) return null;

        if (attachment.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new IllegalArgumentException("첨부파일은 20MB 이하여야 합니다.");
        }

        String originalName = attachment.getOriginalFilename();
        String safeName = (originalName == null || originalName.isBlank()) ? "file.bin" : originalName.replaceAll("[^a-zA-Z0-9._-]", "_");

        String ext = "";
        int dot = safeName.lastIndexOf('.');
        if (dot >= 0 && dot < safeName.length() - 1) {
            ext = safeName.substring(dot + 1).toLowerCase(Locale.ROOT);
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않는 파일 확장자입니다.");
        }

        String contentType = attachment.getContentType();
        Set<String> allowedTypes = ALLOWED_CONTENT_TYPES_BY_EXT.getOrDefault(ext, Set.of());
        String normalizedType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        boolean typeAllowed = allowedTypes.isEmpty() || allowedTypes.stream().anyMatch(normalizedType::startsWith);
        if (!typeAllowed) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        try {
            String storedName = UUID.randomUUID() + "_" + safeName;

            Path dir = Paths.get(uploadDir, "submissions").toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(storedName).normalize();
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }
            Files.copy(attachment.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return storedName;
        } catch (IOException e) {
            throw new RuntimeException("첨부파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
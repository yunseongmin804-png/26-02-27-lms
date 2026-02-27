package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.domain.course.Course;
import com.example.lms.service.course.CourseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private static final Map<Long, String> COURSE_VIDEO_URLS = Map.of(
            101L, "https://www.youtube.com/embed/HWQ5s7jTt8Q",
            102L, "https://www.youtube.com/embed/1fueZCTYkpA",
            103L, "https://www.youtube.com/embed/2L2lnxIcNmo"
    );

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String courses(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String message,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model,
                          HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        CourseService.CoursePage coursePage = courseService.searchCourses(keyword, category, page, size);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("courses", coursePage.items());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("page", coursePage.page());
        model.addAttribute("size", coursePage.size());
        model.addAttribute("totalCount", coursePage.totalCount());
        model.addAttribute("totalPages", coursePage.totalPages());
        model.addAttribute("message", message);
        return "courses/list";
    }

    @GetMapping("/{courseId}")
    public String detail(@PathVariable Long courseId, Model model, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Course course = courseService.getCourse(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("course", course);
        return "courses/detail";
    }

    @GetMapping("/{courseId}/learn")
    public String learn(@PathVariable Long courseId,
                        @RequestParam(required = false) String message,
                        HttpSession session,
                        Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Course course = courseService.getCourse(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        Quiz quiz = quizFor(courseId);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("course", course);
        model.addAttribute("videoUrl", COURSE_VIDEO_URLS.getOrDefault(courseId, "https://www.youtube.com/embed/dQw4w9WgXcQ"));
        model.addAttribute("quiz", quiz);
        model.addAttribute("message", message);
        return "courses/learn";
    }

    @PostMapping("/{courseId}/quiz")
    public String submitQuiz(@PathVariable Long courseId,
                             @RequestParam Integer answer,
                             HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        if (loginUser == null) return "redirect:/login";

        Quiz quiz = quizFor(courseId);
        boolean correct = answer != null && answer == quiz.correctIndex();
        String msg = correct ? "정답입니다!" : "오답입니다. 다시 시도해보세요.";
        return "redirect:/courses/" + courseId + "/learn?message=" + msg;
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        model.addAttribute("loginUser", loginUser);
        return "courses/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String category,
                         @RequestParam String instructorName,
                         @RequestParam Integer capacity,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        courseService.createCourse(loginUser, title, category, instructorName, capacity);
        return "redirect:/courses?message=강의가+등록되었습니다";
    }

    @GetMapping("/{courseId}/edit")
    public String editForm(@PathVariable Long courseId, HttpSession session, Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Course course = courseService.getCourse(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("course", course);
        return "courses/edit";
    }

    @PostMapping("/{courseId}/edit")
    public String edit(@PathVariable Long courseId,
                       @RequestParam String title,
                       @RequestParam String category,
                       @RequestParam String instructorName,
                       @RequestParam Integer capacity,
                       HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        courseService.updateCourse(loginUser, courseId, title, category, instructorName, capacity);
        return "redirect:/courses/" + courseId + "?message=강의가+수정되었습니다";
    }

    @PostMapping("/{courseId}/delete")
    public String delete(@PathVariable Long courseId, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        courseService.deleteCourse(loginUser, courseId);
        return "redirect:/courses?message=강의가+삭제되었습니다";
    }

    @PostMapping("/bulk")
    public String bulkCreate(@RequestParam(defaultValue = "10") int count,
                             @RequestParam(defaultValue = "샘플 강의") String titlePrefix,
                             @RequestParam(defaultValue = "강사1") String instructorName,
                             @RequestParam(defaultValue = "IT/외국어") String category,
                             @RequestParam(defaultValue = "30") Integer capacity,
                             HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        int created = courseService.createBulkCourses(loginUser, count, titlePrefix, instructorName, category, capacity);
        return "redirect:/courses?message=" + created + "+개+강의+일괄+생성";
    }

    private Quiz quizFor(Long courseId) {
        if (courseId == 101L) {
            return new Quiz("Spring Boot의 핵심 장점은?", new String[]{"설정 자동화", "수동 XML 설정만 가능", "JSP 전용", "MySQL 내장"}, 0);
        }
        if (courseId == 102L) {
            return new Quiz("영어 회화 학습에 가장 중요한 것은?", new String[]{"암기만", "반복 말하기", "문법만", "시험 점수만"}, 1);
        }
        return new Quiz("학습 효과를 높이는 방법은?", new String[]{"벼락치기", "꾸준한 복습", "무학습", "복사"}, 1);
    }

    private record Quiz(String question, String[] options, int correctIndex) {}
}

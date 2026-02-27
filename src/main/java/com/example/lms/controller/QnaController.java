package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.qna.QnaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/qna")
public class QnaController {

    private final QnaService qnaService;

    public QnaController(QnaService qnaService) {
        this.qnaService = qnaService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String message,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       HttpSession session,
                       Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        boolean manager = loginUser.canManageCourses();
        QnaService.QnaPage qnaPage = qnaService.getQnaPage(manager, loginUser.id(), keyword, sort, page, size);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("qnaList", qnaPage.items());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("page", qnaPage.page());
        model.addAttribute("size", qnaPage.size());
        model.addAttribute("totalCount", qnaPage.totalCount());
        model.addAttribute("totalPages", qnaPage.totalPages());
        model.addAttribute("message", message);
        return "qna/list";
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String title,
                      @RequestParam String content,
                      HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        qnaService.ask(loginUser, title, content);
        return "redirect:/qna?message=질문이+등록되었습니다";
    }

    @PostMapping("/answer")
    public String answer(@RequestParam Long questionId,
                         @RequestParam String content,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        qnaService.answer(loginUser, questionId, content);
        return "redirect:/qna?message=답변이+등록되었습니다";
    }
}

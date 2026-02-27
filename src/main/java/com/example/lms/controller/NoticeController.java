package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.domain.notice.Notice;
import com.example.lms.service.notice.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String message,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String draftTitle,
                       @RequestParam(required = false) String draftContent,
                       HttpSession session,
                       Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        NoticeService.NoticePage noticePage = noticeService.getNotices(keyword, sort, page, size);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("notices", noticePage.items());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("page", noticePage.page());
        model.addAttribute("size", noticePage.size());
        model.addAttribute("totalCount", noticePage.totalCount());
        model.addAttribute("totalPages", noticePage.totalPages());
        model.addAttribute("draftTitle", draftTitle == null ? "" : draftTitle);
        model.addAttribute("draftContent", draftContent == null ? "" : draftContent);
        model.addAttribute("message", message);
        return "notices/list";
    }

    @GetMapping("/{noticeId}")
    public String detail(@PathVariable Long noticeId,
                         @RequestParam(required = false) String message,
                         HttpSession session,
                         Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Notice notice = noticeService.getNotice(noticeId).orElse(null);
        if (notice == null) return "redirect:/notices";

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("notice", notice);
        model.addAttribute("message", message);
        return "notices/detail";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        noticeService.createNotice(loginUser, title, content);
        return "redirect:/notices?message=공지사항이+등록되었습니다";
    }

    @GetMapping("/{noticeId}/edit")
    public String editForm(@PathVariable Long noticeId, HttpSession session, Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Notice notice = noticeService.getNotice(noticeId).orElse(null);
        if (notice == null) return "redirect:/notices";

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("notice", notice);
        return "notices/edit";
    }

    @PostMapping("/{noticeId}/edit")
    public String edit(@PathVariable Long noticeId,
                       @RequestParam String title,
                       @RequestParam String content,
                       HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        noticeService.updateNotice(loginUser, noticeId, title, content);
        return "redirect:/notices/" + noticeId + "?message=공지사항이+수정되었습니다";
    }

    @PostMapping("/{noticeId}/delete")
    public String delete(@PathVariable Long noticeId, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        noticeService.deleteNotice(loginUser, noticeId);
        return "redirect:/notices?message=공지사항이+삭제되었습니다";
    }
}

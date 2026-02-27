package com.example.lms.api;

import com.example.lms.api.dto.ApiResponse;
import com.example.lms.api.dto.NoticeResponse;
import com.example.lms.api.dto.PageResponse;
import com.example.lms.domain.notice.Notice;
import com.example.lms.service.notice.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeApiController {

    private final NoticeService noticeService;

    public NoticeApiController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NoticeResponse>> list(@RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "latest") String sort,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        NoticeService.NoticePage noticePage = noticeService.getNotices(keyword, sort, page, size);
        PageResponse<NoticeResponse> response = new PageResponse<>(
                noticePage.items().stream().map(this::toResponse).toList(),
                noticePage.page(),
                noticePage.size(),
                noticePage.totalCount(),
                noticePage.totalPages()
        );
        return ApiResponse.ok(response);
    }

    private NoticeResponse toResponse(Notice n) {
        return new NoticeResponse(n.getId(), n.getTitle(), n.getContent(), n.getAuthorName(), n.getCreatedAt());
    }
}

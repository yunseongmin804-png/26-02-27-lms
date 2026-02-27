package com.example.lms.service.notice;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.notice.Notice;
import com.example.lms.repository.notice.NoticeJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.requiredText;

@Service
@Transactional(readOnly = true)
public class NoticeService {

    public record NoticePage(List<Notice> items, int page, int size, int totalCount, int totalPages) {}

    private final NoticeJdbcRepository noticeJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public NoticeService(NoticeJdbcRepository noticeJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.noticeJdbcRepository = noticeJdbcRepository;
        this.auditLogService = auditLogService;
    }

    public NoticePage getNotices(String keyword, String sort, int page, int size) {
        int validPage = Math.max(page, 1);
        int validSize = Math.max(size, 1);

        int totalCount = noticeJdbcRepository.countByKeyword(keyword);
        int totalPages = Math.max((int) Math.ceil(totalCount / (double) validSize), 1);
        int safePage = Math.min(validPage, totalPages);

        int offset = (safePage - 1) * validSize;
        List<Notice> items = noticeJdbcRepository.searchPaged(keyword, sort, validSize, offset);
        return new NoticePage(items, safePage, validSize, totalCount, totalPages);
    }

    public Optional<Notice> getNotice(Long id) {
        return noticeJdbcRepository.findById(id);
    }

    @Transactional
    public void createNotice(LoginUser actor, String title, String content) {
        requireManager(actor);
        String t = requiredText(title, "제목", 200);
        String c = requiredText(content, "내용", 5000);

        Long id = noticeJdbcRepository.nextId();
        noticeJdbcRepository.save(new Notice(id, t, c, actor.name(), LocalDateTime.now()));
        auditLogService.log(actor, "NOTICE_CREATE", "NOTICE", String.valueOf(id), t);
    }

    @Transactional
    public boolean createNoticeIfNotExistsToday(LoginUser actor, String title, String content) {
        requireManager(actor);
        String t = requiredText(title, "제목", 200);
        String c = requiredText(content, "내용", 5000);

        // 1) 같은 제목의 당일 중복 방지
        if (noticeJdbcRepository.existsTodayByTitle(t)) {
            return false;
        }
        // 2) 같은 제목+본문의 최근 24시간 중복 방지
        if (noticeJdbcRepository.existsRecentByTitleAndContent(t, c, 24)) {
            return false;
        }

        Long id = noticeJdbcRepository.nextId();
        noticeJdbcRepository.save(new Notice(id, t, c, actor.name(), LocalDateTime.now()));
        auditLogService.log(actor, "NOTICE_CREATE", "NOTICE", String.valueOf(id), t);
        return true;
    }

    @Transactional
    public void updateNotice(LoginUser actor, Long id, String title, String content) {
        requireManager(actor);
        if (id == null) throw new IllegalArgumentException("공지 ID가 필요합니다.");
        String t = requiredText(title, "제목", 200);
        String c = requiredText(content, "내용", 5000);

        Notice old = noticeJdbcRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));
        noticeJdbcRepository.update(new Notice(old.getId(), t, c, old.getAuthorName(), old.getCreatedAt()));
        auditLogService.log(actor, "NOTICE_UPDATE", "NOTICE", String.valueOf(id), t);
    }

    @Transactional
    public void deleteNotice(LoginUser actor, Long id) {
        requireManager(actor);
        if (id == null) throw new IllegalArgumentException("공지 ID가 필요합니다.");
        noticeJdbcRepository.deleteById(id);
        auditLogService.log(actor, "NOTICE_DELETE", "NOTICE", String.valueOf(id), null);
    }
}

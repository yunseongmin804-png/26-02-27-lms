package com.example.lms.service.qna;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.qna.QnaAnswer;
import com.example.lms.domain.qna.QnaQuestion;
import com.example.lms.repository.qna.QnaJdbcRepository;
import com.example.lms.repository.qna.QnaView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.lms.common.security.AccessControl.requireLogin;
import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.requiredText;

@Service
@Transactional(readOnly = true)
public class QnaService {

    public record QnaPage(List<QnaView> items, int page, int size, int totalCount, int totalPages) {}

    private final QnaJdbcRepository qnaJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public QnaService(QnaJdbcRepository qnaJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.qnaJdbcRepository = qnaJdbcRepository;
        this.auditLogService = auditLogService;
    }

    public QnaPage getQnaPage(boolean manager, Long studentId, String keyword, String sort, int page, int size) {
        int validPage = Math.max(page, 1);
        int validSize = Math.max(size, 1);

        int totalCount = manager
                ? qnaJdbcRepository.countAllByKeyword(keyword)
                : qnaJdbcRepository.countByStudentAndKeyword(studentId, keyword);

        int totalPages = Math.max((int) Math.ceil(totalCount / (double) validSize), 1);
        int safePage = Math.min(validPage, totalPages);
        int offset = (safePage - 1) * validSize;

        List<QnaView> items = manager
                ? qnaJdbcRepository.searchAllPaged(keyword, sort, validSize, offset)
                : qnaJdbcRepository.searchByStudentPaged(studentId, keyword, sort, validSize, offset);

        return new QnaPage(items, safePage, validSize, totalCount, totalPages);
    }

    public int countUnanswered() {
        return qnaJdbcRepository.countUnanswered();
    }

    @Transactional
    public void ask(LoginUser actor, String title, String content) {
        requireLogin(actor);
        String t = requiredText(title, "질문 제목", 200);
        String c = requiredText(content, "질문 내용", 5000);

        Long id = qnaJdbcRepository.nextQuestionId();
        qnaJdbcRepository.saveQuestion(new QnaQuestion(id, actor.id(), t, c));
        auditLogService.log(actor, "QNA_ASK", "QNA_QUESTION", String.valueOf(id), t);
    }

    @Transactional
    public void answer(LoginUser actor, Long questionId, String content) {
        requireManager(actor);
        if (questionId == null) throw new IllegalArgumentException("질문 ID가 필요합니다.");
        String c = requiredText(content, "답변 내용", 5000);

        Long id = qnaJdbcRepository.nextAnswerId();
        qnaJdbcRepository.saveAnswer(new QnaAnswer(id, questionId, actor.name(), c));
        auditLogService.log(actor, "QNA_ANSWER", "QNA_QUESTION", String.valueOf(questionId), null);
    }
}

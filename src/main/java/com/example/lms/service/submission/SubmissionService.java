package com.example.lms.service.submission;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.submission.Submission;
import com.example.lms.repository.submission.SubmissionJdbcRepository;
import com.example.lms.repository.submission.SubmissionView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.requiredText;
import static com.example.lms.common.validation.ValidationUtil.score;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionJdbcRepository submissionJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public SubmissionService(SubmissionJdbcRepository submissionJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.submissionJdbcRepository = submissionJdbcRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void submitOrResubmit(Long assignmentId, Long studentId, String content, String attachmentPath) {
        if (assignmentId == null) throw new IllegalArgumentException("과제 ID가 필요합니다.");
        if (studentId == null) throw new IllegalArgumentException("학생 ID가 필요합니다.");
        String c = requiredText(content, "제출 내용", 5000);

        if (submissionJdbcRepository.findIdByAssignmentAndStudent(assignmentId, studentId).isPresent()) {
            submissionJdbcRepository.updateContentAndAttachment(assignmentId, studentId, c, attachmentPath);
            auditLogService.log(null, "SUBMISSION_RESUBMIT", "ASSIGNMENT", String.valueOf(assignmentId), "studentId=" + studentId);
            return;
        }

        Long id = submissionJdbcRepository.nextId();
        submissionJdbcRepository.save(new Submission(id, assignmentId, studentId, c, attachmentPath, null, null));
        auditLogService.log(null, "SUBMISSION_CREATE", "ASSIGNMENT", String.valueOf(assignmentId), "studentId=" + studentId);
    }

    @Transactional
    public void grade(LoginUser actor, Long submissionId, Integer score, String feedback) {
        requireManager(actor);
        if (submissionId == null) throw new IllegalArgumentException("제출 ID가 필요합니다.");
        int s = score(score);
        String f = feedback == null ? "" : feedback.trim();
        if (f.length() > 500) throw new IllegalArgumentException("피드백은 500자 이하여야 합니다.");

        submissionJdbcRepository.grade(submissionId, s, f);
        auditLogService.log(actor, "SUBMISSION_GRADE", "SUBMISSION", String.valueOf(submissionId), "score=" + s);
    }

    public List<SubmissionView> getAllSubmissions() {
        return submissionJdbcRepository.findAllViews();
    }

    public List<SubmissionView> getMySubmissions(Long studentId) {
        return submissionJdbcRepository.findViewsByStudentId(studentId);
    }

    public int countUngraded() {
        return submissionJdbcRepository.countUngraded();
    }
}

package com.example.lms.service.assignment;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.assignment.Assignment;
import com.example.lms.repository.assignment.AssignmentJdbcRepository;
import com.example.lms.repository.assignment.AssignmentView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.requiredText;

@Service
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentJdbcRepository assignmentJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public AssignmentService(AssignmentJdbcRepository assignmentJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.assignmentJdbcRepository = assignmentJdbcRepository;
        this.auditLogService = auditLogService;
    }

    public List<AssignmentView> getAssignments(Long courseId) {
        if (courseId == null) {
            return assignmentJdbcRepository.findAllViews();
        }
        return assignmentJdbcRepository.findViewsByCourseId(courseId);
    }

    public Optional<AssignmentView> getAssignment(Long assignmentId) {
        return assignmentJdbcRepository.findViewById(assignmentId);
    }

    @Transactional
    public void createAssignment(LoginUser actor, Long courseId, String title, String description, LocalDate dueDate) {
        requireManager(actor);
        if (courseId == null) throw new IllegalArgumentException("강의를 선택해주세요.");
        String t = requiredText(title, "과제 제목", 200);
        String d = description == null ? "" : description.trim();

        Long id = assignmentJdbcRepository.nextId();
        assignmentJdbcRepository.save(new Assignment(id, courseId, t, d, dueDate));
        auditLogService.log(actor, "ASSIGNMENT_CREATE", "ASSIGNMENT", String.valueOf(id), t);
    }

    public boolean isSubmissionOpen(Long assignmentId, LocalDate today) {
        Optional<AssignmentView> assignment = getAssignment(assignmentId);
        if (assignment.isEmpty()) return false;

        LocalDate dueDate = assignment.get().dueDate();
        if (dueDate == null) return true;
        return !today.isAfter(dueDate);
    }

    public int countDueInDays(int days) {
        return assignmentJdbcRepository.countDueInDays(days);
    }

    public int countOverdue() {
        return assignmentJdbcRepository.countOverdue();
    }

    @Transactional
    public int createBulkAssignments(LoginUser actor, Long courseId, int count, String titlePrefix, Integer dueAfterDays) {
        requireManager(actor);
        if (courseId == null) throw new IllegalArgumentException("강의를 선택해주세요.");
        int safeCount = Math.max(1, Math.min(count, 100));
        String prefix = requiredText(titlePrefix, "과제 제목 접두사", 100);
        int startAfter = Math.max(0, Math.min(dueAfterDays == null ? 1 : dueAfterDays, 365));

        int created = 0;
        for (int i = 1; i <= safeCount; i++) {
            Long id = assignmentJdbcRepository.nextId();
            LocalDate due = LocalDate.now().plusDays(startAfter + i);
            assignmentJdbcRepository.save(new Assignment(id, courseId, prefix + " " + i, "일괄 생성 과제", due));
            created++;
        }
        auditLogService.log(actor, "ASSIGNMENT_BULK_CREATE", "ASSIGNMENT", null, "count=" + created + ",courseId=" + courseId);
        return created;
    }
}

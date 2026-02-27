package com.example.lms.service.audit;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.audit.AuditLog;
import com.example.lms.repository.audit.AuditLogJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogJdbcRepository auditLogJdbcRepository;

    public AuditLogService(AuditLogJdbcRepository auditLogJdbcRepository) {
        this.auditLogJdbcRepository = auditLogJdbcRepository;
    }

    @Transactional
    public void log(LoginUser actor, String action, String targetType, String targetId, String detail) {
        Long id = auditLogJdbcRepository.nextId();
        Long actorId = actor == null ? null : actor.id();
        String actorName = actor == null ? "system" : actor.name();
        String actorRole = actor == null ? "SYSTEM" : actor.role();

        auditLogJdbcRepository.save(new AuditLog(
                id,
                actorId,
                actorName,
                actorRole,
                action,
                targetType,
                targetId,
                detail,
                LocalDateTime.now()
        ));
    }

    public List<AuditLog> getRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return auditLogJdbcRepository.findRecent(safeLimit);
    }
}

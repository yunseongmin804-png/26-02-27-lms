package com.example.lms.service.user;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.user.User;
import com.example.lms.repository.user.UserJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.lms.common.security.AccessControl.requireAdmin;
import static com.example.lms.common.validation.ValidationUtil.*;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserJdbcRepository userJdbcRepository;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public UserService(UserJdbcRepository userJdbcRepository, com.example.lms.service.audit.AuditLogService auditLogService) {
        this.userJdbcRepository = userJdbcRepository;
        this.auditLogService = auditLogService;
    }

    public List<User> getUsers() {
        return userJdbcRepository.findAll();
    }

    public Optional<User> getUser(Long id) {
        return userJdbcRepository.findById(id);
    }

    @Transactional
    public void createUser(LoginUser actor, String email, String name, String role) {
        requireAdmin(actor);

        String validEmail = email(email);
        String validName = requiredText(name, "이름", 100);
        String validRole = role(role);

        Long id = userJdbcRepository.nextId();
        userJdbcRepository.save(new User(id, validEmail, validName, validRole));
        auditLogService.log(actor, "USER_CREATE", "USER", String.valueOf(id), "email=" + validEmail + ",role=" + validRole);
    }

    @Transactional
    public int createBulkStudents(LoginUser actor, int count, String namePrefix) {
        requireAdmin(actor);
        int safeCount = Math.max(1, Math.min(count, 200));
        String prefix = requiredText(namePrefix, "이름 접두사", 30);

        int created = 0;
        for (int i = 1; i <= safeCount; i++) {
            Long id = userJdbcRepository.nextId();
            String name = prefix + i;
            String email = (prefix.toLowerCase() + id + "@demo.local").replaceAll("\\s+", "");
            userJdbcRepository.save(new User(id, email, name, "STUDENT"));
            created++;
        }
        auditLogService.log(actor, "USER_BULK_CREATE", "USER", null, "count=" + created + ",prefix=" + prefix);
        return created;
    }

    public int countUsers() {
        return userJdbcRepository.count();
    }
}

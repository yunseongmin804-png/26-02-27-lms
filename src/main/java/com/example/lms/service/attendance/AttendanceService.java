package com.example.lms.service.attendance;

import com.example.lms.auth.LoginUser;
import com.example.lms.domain.attendance.Attendance;
import com.example.lms.repository.attendance.AbsentTopRow;
import com.example.lms.repository.attendance.AttendanceJdbcRepository;
import com.example.lms.repository.attendance.AttendanceView;
import com.example.lms.repository.attendance.StudentAttendanceSummaryRow;
import com.example.lms.service.enrollment.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.example.lms.common.security.AccessControl.requireManager;
import static com.example.lms.common.validation.ValidationUtil.requiredText;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceJdbcRepository attendanceJdbcRepository;
    private final EnrollmentService enrollmentService;
    private final com.example.lms.service.audit.AuditLogService auditLogService;

    public AttendanceService(AttendanceJdbcRepository attendanceJdbcRepository,
                             EnrollmentService enrollmentService,
                             com.example.lms.service.audit.AuditLogService auditLogService) {
        this.attendanceJdbcRepository = attendanceJdbcRepository;
        this.enrollmentService = enrollmentService;
        this.auditLogService = auditLogService;
    }

    public List<AttendanceView> getAttendance(LoginUser actor, Long courseId, LocalDate attendanceDate) {
        return getAttendance(actor, courseId, attendanceDate, false);
    }

    public List<AttendanceView> getAttendance(LoginUser actor, Long courseId, LocalDate attendanceDate, boolean absentOnly) {
        if (actor.canManageCourses()) {
            return attendanceJdbcRepository.findAllViews(courseId, attendanceDate, absentOnly);
        }
        return attendanceJdbcRepository.findViewsByUserId(actor.id());
    }

    @Transactional
    public void markAttendance(LoginUser actor, Long enrollmentId, LocalDate attendanceDate, String status) {
        requireManager(actor);
        if (enrollmentId == null) throw new IllegalArgumentException("수강신청 ID가 필요합니다.");
        if (attendanceDate == null) throw new IllegalArgumentException("출석 날짜가 필요합니다.");

        String normalized = requiredText(status, "출석 상태", 20).toUpperCase();
        if (!List.of("PRESENT", "LATE", "ABSENT").contains(normalized)) {
            throw new IllegalArgumentException("출석 상태는 PRESENT/LATE/ABSENT 중 하나여야 합니다.");
        }

        if (attendanceJdbcRepository.existsByEnrollmentAndDate(enrollmentId, attendanceDate)) {
            throw new IllegalStateException("같은 날짜 출석은 중복 기록할 수 없습니다.");
        }

        Long id = attendanceJdbcRepository.nextId();
        attendanceJdbcRepository.save(new Attendance(id, enrollmentId, attendanceDate, normalized));
        auditLogService.log(actor, "ATTENDANCE_MARK", "ATTENDANCE", String.valueOf(id),
                "enrollmentId=" + enrollmentId + ",date=" + attendanceDate + ",status=" + normalized);
    }

    @Transactional
    public int markBulkByCourse(LoginUser actor, Long courseId, LocalDate attendanceDate, String status) {
        requireManager(actor);
        if (courseId == null) throw new IllegalArgumentException("강의 ID가 필요합니다.");
        if (attendanceDate == null) throw new IllegalArgumentException("출석 날짜가 필요합니다.");

        String normalized = requiredText(status, "출석 상태", 20).toUpperCase();
        if (!List.of("PRESENT", "LATE", "ABSENT").contains(normalized)) {
            throw new IllegalArgumentException("출석 상태는 PRESENT/LATE/ABSENT 중 하나여야 합니다.");
        }

        List<Long> enrollmentIds = enrollmentService.getEnrollmentIdsByCourse(courseId);
        int created = 0;
        for (Long enrollmentId : enrollmentIds) {
            if (attendanceJdbcRepository.existsByEnrollmentAndDate(enrollmentId, attendanceDate)) {
                continue;
            }
            Long id = attendanceJdbcRepository.nextId();
            attendanceJdbcRepository.save(new Attendance(id, enrollmentId, attendanceDate, normalized));
            created++;
        }

        auditLogService.log(actor, "ATTENDANCE_BULK_MARK", "COURSE", String.valueOf(courseId),
                "date=" + attendanceDate + ",status=" + normalized + ",created=" + created);
        return created;
    }

    @Transactional
    public int upsertBulkByEntries(LoginUser actor, LocalDate attendanceDate, java.util.Map<Long, String> statusByEnrollmentId) {
        requireManager(actor);
        if (attendanceDate == null) throw new IllegalArgumentException("출석 날짜가 필요합니다.");
        if (statusByEnrollmentId == null || statusByEnrollmentId.isEmpty()) {
            throw new IllegalArgumentException("출석 대상이 없습니다.");
        }

        int affected = 0;
        for (java.util.Map.Entry<Long, String> entry : statusByEnrollmentId.entrySet()) {
            Long enrollmentId = entry.getKey();
            String normalized = requiredText(entry.getValue(), "출석 상태", 20).toUpperCase();
            if (!List.of("PRESENT", "LATE", "ABSENT").contains(normalized)) {
                throw new IllegalArgumentException("출석 상태는 PRESENT/LATE/ABSENT 중 하나여야 합니다.");
            }

            java.util.Optional<Long> existingId = attendanceJdbcRepository.findIdByEnrollmentAndDate(enrollmentId, attendanceDate);
            if (existingId.isPresent()) {
                attendanceJdbcRepository.updateStatus(existingId.get(), normalized);
            } else {
                Long id = attendanceJdbcRepository.nextId();
                attendanceJdbcRepository.save(new Attendance(id, enrollmentId, attendanceDate, normalized));
            }
            affected++;
        }

        auditLogService.log(actor, "ATTENDANCE_BULK_UPSERT", "ATTENDANCE", null,
                "date=" + attendanceDate + ",affected=" + affected);
        return affected;
    }

    @Transactional
    public void updateStatus(LoginUser actor, Long attendanceId, String status) {
        requireManager(actor);
        if (attendanceId == null) throw new IllegalArgumentException("출석 ID가 필요합니다.");

        String normalized = requiredText(status, "출석 상태", 20).toUpperCase();
        if (!List.of("PRESENT", "LATE", "ABSENT").contains(normalized)) {
            throw new IllegalArgumentException("출석 상태는 PRESENT/LATE/ABSENT 중 하나여야 합니다.");
        }

        int updated = attendanceJdbcRepository.updateStatus(attendanceId, normalized);
        if (updated == 0) throw new IllegalArgumentException("존재하지 않는 출석입니다.");
        auditLogService.log(actor, "ATTENDANCE_UPDATE", "ATTENDANCE", String.valueOf(attendanceId), "status=" + normalized);
    }

    @Transactional
    public void deleteAttendance(LoginUser actor, Long attendanceId) {
        requireManager(actor);
        if (attendanceId == null) throw new IllegalArgumentException("출석 ID가 필요합니다.");

        int deleted = attendanceJdbcRepository.deleteById(attendanceId);
        if (deleted == 0) throw new IllegalArgumentException("존재하지 않는 출석입니다.");
        auditLogService.log(actor, "ATTENDANCE_DELETE", "ATTENDANCE", String.valueOf(attendanceId), null);
    }

    public java.util.List<AttendanceView> getAttendanceByRange(LoginUser actor, Long courseId, LocalDate startDate, LocalDate endDate, boolean absentOnly) {
        if (!actor.canManageCourses()) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        return attendanceJdbcRepository.findAllViewsByDateRange(courseId, startDate, endDate, absentOnly);
    }

    public java.util.List<StudentAttendanceSummaryRow> getStudentSummaryByCourse(LoginUser actor, Long courseId, LocalDate startDate, LocalDate endDate) {
        if (!actor.canManageCourses()) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        return attendanceJdbcRepository.summarizeByCourse(courseId, startDate, endDate);
    }

    public java.util.List<AbsentTopRow> getTopAbsentees(LoginUser actor, int limit, LocalDate startDate, LocalDate endDate) {
        if (!actor.canManageCourses()) {
            throw new IllegalStateException("권한이 없습니다.");
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return attendanceJdbcRepository.findTopAbsentees(safeLimit, startDate, endDate);
    }

    public java.util.List<AbsentTopRow> getRiskAbsentees(LoginUser actor, double thresholdPercent, int limit) {
        java.util.List<AbsentTopRow> rows = getTopAbsentees(actor, 100, null, null);
        double t = Math.max(0d, Math.min(thresholdPercent, 100d));
        return rows.stream()
                .filter(r -> r.absentRate() >= t)
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
    }

    public int countRiskAbsentees(LoginUser actor, double thresholdPercent) {
        return getRiskAbsentees(actor, thresholdPercent, 100).size();
    }

    public java.util.Map<Long, Double> getEnrollmentRates(java.util.List<Long> enrollmentIds) {
        java.util.Map<Long, Double> rates = new java.util.HashMap<>();
        for (Long enrollmentId : enrollmentIds) {
            int present = attendanceJdbcRepository.countPresentByEnrollmentId(enrollmentId);
            int total = attendanceJdbcRepository.countTotalByEnrollmentId(enrollmentId);
            double rate = total == 0 ? 0d : (present * 100.0 / total);
            rates.put(enrollmentId, rate);
        }
        return rates;
    }

    public AttendanceSummary getSummary(LoginUser actor) {
        if (actor.canManageCourses()) {
            int todayPresent = attendanceJdbcRepository.countTodayPresent();
            int todayTotal = attendanceJdbcRepository.countTodayTotal();
            double rate = todayTotal == 0 ? 0d : (todayPresent * 100.0 / todayTotal);
            return new AttendanceSummary(todayPresent, todayTotal, rate);
        }

        int present = attendanceJdbcRepository.countPresentByUserId(actor.id());
        int total = attendanceJdbcRepository.countTotalByUserId(actor.id());
        double rate = total == 0 ? 0d : (present * 100.0 / total);
        return new AttendanceSummary(present, total, rate);
    }

    public record AttendanceSummary(int presentCount, int totalCount, double attendanceRate) {}
}

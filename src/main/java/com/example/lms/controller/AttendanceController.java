package com.example.lms.controller;

import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.attendance.AttendanceService;
import com.example.lms.service.course.CourseService;
import com.example.lms.service.enrollment.EnrollmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    public AttendanceController(AttendanceService attendanceService,
                                EnrollmentService enrollmentService,
                                CourseService courseService) {
        this.attendanceService = attendanceService;
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String message,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) String date,
                       @RequestParam(required = false) String month,
                       @RequestParam(defaultValue = "ALL") String issueType,
                       HttpSession session,
                       Model model) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate parsedDate = (date == null || date.isBlank()) ? null : LocalDate.parse(date);
        YearMonth selectedMonth = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("summary", attendanceService.getSummary(loginUser));
        model.addAttribute("attendances", attendanceService.getAttendance(loginUser, courseId, parsedDate));
        model.addAttribute("enrollments", enrollmentService.getAllEnrollments());
        model.addAttribute("courses", courseService.getCourses());
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("issueType", issueType);
        model.addAttribute("defaultAttendanceDate", (date == null || date.isBlank()) ? LocalDate.now().toString() : date);

        var bulkEnrollments = courseId == null ? java.util.List.<com.example.lms.repository.enrollment.EnrollmentView>of() : enrollmentService.getEnrollmentsByCourse(courseId);
        model.addAttribute("bulkEnrollments", bulkEnrollments);
        model.addAttribute("bulkRates", attendanceService.getEnrollmentRates(bulkEnrollments.stream().map(com.example.lms.repository.enrollment.EnrollmentView::getEnrollmentId).toList()));

        List<Integer> monthDays = new ArrayList<>();
        for (int d = 1; d <= selectedMonth.lengthOfMonth(); d++) monthDays.add(d);
        model.addAttribute("monthDays", monthDays);

        Map<Long, Map<Integer, String>> monthlyStatus = new HashMap<>();
        Map<Integer, String> myMonthlyStatus = new HashMap<>();
        if (loginUser.canManageCourses() && courseId != null) {
            LocalDate start = selectedMonth.atDay(1);
            LocalDate end = selectedMonth.atEndOfMonth();
            attendanceService.getAttendanceByRange(loginUser, courseId, start, end, false).forEach(a -> {
                monthlyStatus.computeIfAbsent(a.getEnrollmentId(), k -> new HashMap<>())
                        .put(a.getAttendanceDate().getDayOfMonth(), a.getStatus());
            });
        } else {
            attendanceService.getAttendance(loginUser, null, null).forEach(a -> {
                if (YearMonth.from(a.getAttendanceDate()).equals(selectedMonth)) {
                    myMonthlyStatus.put(a.getAttendanceDate().getDayOfMonth(), a.getStatus());
                }
            });
        }
        model.addAttribute("monthlyStatus", monthlyStatus);
        model.addAttribute("myMonthlyStatus", myMonthlyStatus);

        List<MonthlyIssueRow> monthlyIssues = new ArrayList<>();
        List<String> compareMonths = List.of(selectedMonth.minusMonths(1).toString(), selectedMonth.toString());
        List<MonthlyCompareRow> monthlyCompareRows = new ArrayList<>();

        if (loginUser.canManageCourses() && courseId != null) {
            LocalDate cmpStart = selectedMonth.minusMonths(1).atDay(1);
            LocalDate cmpEnd = selectedMonth.atEndOfMonth();
            var compareAttendance = attendanceService.getAttendanceByRange(loginUser, courseId, cmpStart, cmpEnd, false);

            Map<Long, Map<String, Integer>> absentByMonthByEnrollment = new HashMap<>();
            Map<Long, Map<String, Integer>> lateByMonthByEnrollment = new HashMap<>();

            for (var a : compareAttendance) {
                String ym = YearMonth.from(a.getAttendanceDate()).toString();
                if ("ABSENT".equals(a.getStatus())) {
                    absentByMonthByEnrollment
                            .computeIfAbsent(a.getEnrollmentId(), k -> new HashMap<>())
                            .merge(ym, 1, Integer::sum);
                }
                if ("LATE".equals(a.getStatus())) {
                    lateByMonthByEnrollment
                            .computeIfAbsent(a.getEnrollmentId(), k -> new HashMap<>())
                            .merge(ym, 1, Integer::sum);
                }
            }

            for (var e : bulkEnrollments) {
                Map<Integer, String> row = monthlyStatus.getOrDefault(e.getEnrollmentId(), Map.of());
                int absentCount = 0;
                int lateCount = 0;
                for (String s : row.values()) {
                    if ("ABSENT".equals(s)) absentCount++;
                    if ("LATE".equals(s)) lateCount++;
                }
                boolean include = switch (issueType.toUpperCase()) {
                    case "ABSENT" -> absentCount > 0;
                    case "LATE" -> lateCount > 0;
                    default -> (absentCount > 0 || lateCount > 0);
                };
                if (include) {
                    monthlyIssues.add(new MonthlyIssueRow(e.getUserName(), e.getCourseTitle(), absentCount, lateCount));
                }

                Map<String, Integer> absentMap = new LinkedHashMap<>();
                Map<String, Integer> lateMap = new LinkedHashMap<>();
                for (String m : compareMonths) {
                    absentMap.put(m, absentByMonthByEnrollment.getOrDefault(e.getEnrollmentId(), Map.of()).getOrDefault(m, 0));
                    lateMap.put(m, lateByMonthByEnrollment.getOrDefault(e.getEnrollmentId(), Map.of()).getOrDefault(m, 0));
                }
                monthlyCompareRows.add(new MonthlyCompareRow(e.getUserName(), e.getCourseTitle(), absentMap, lateMap));
            }
        }
        model.addAttribute("monthlyIssues", monthlyIssues);
        model.addAttribute("compareMonths", compareMonths);
        model.addAttribute("monthlyCompareRows", monthlyCompareRows);

        model.addAttribute("message", message);
        return "attendance/list";
    }

    @PostMapping
    public String mark(@RequestParam Long enrollmentId,
                       @RequestParam String attendanceDate,
                       @RequestParam String status,
                       HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        attendanceService.markAttendance(loginUser, enrollmentId, LocalDate.parse(attendanceDate), status);
        return "redirect:/attendance?message=출석이+기록되었습니다";
    }

    @PostMapping("/bulk")
    public String bulkMark(@RequestParam Long courseId,
                           @RequestParam String attendanceDate,
                           @RequestParam String status,
                           HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        int created = attendanceService.markBulkByCourse(loginUser, courseId, LocalDate.parse(attendanceDate), status);
        return "redirect:/attendance?message=" + created + "+건+일괄+출석+기록되었습니다";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) Long courseId,
                                             @RequestParam(required = false) String date,
                                             @RequestParam(defaultValue = "false") boolean absentOnly,
                                             HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate parsedDate = (date == null || date.isBlank()) ? null : LocalDate.parse(date);

        StringBuilder sb = new StringBuilder();
        sb.append("attendanceId,studentName,courseTitle,date,status\n");
        attendanceService.getAttendance(loginUser, courseId, parsedDate, absentOnly).forEach(a -> {
            sb.append(a.getAttendanceId()).append(',')
                    .append(escapeCsv(a.getUserName())).append(',')
                    .append(escapeCsv(a.getCourseTitle())).append(',')
                    .append(a.getAttendanceDate()).append(',')
                    .append(a.getStatus())
                    .append('\n');
        });

        String filename = "attendance-" + (parsedDate == null ? "all" : parsedDate) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export-range")
    public ResponseEntity<byte[]> exportRangeCsv(@RequestParam(required = false) Long courseId,
                                                  @RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate,
                                                  @RequestParam(defaultValue = "false") boolean absentOnly,
                                                  HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate start = (startDate == null || startDate.isBlank()) ? null : LocalDate.parse(startDate);
        LocalDate end = (endDate == null || endDate.isBlank()) ? null : LocalDate.parse(endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("attendanceId,studentName,courseTitle,date,status\n");
        attendanceService.getAttendanceByRange(loginUser, courseId, start, end, absentOnly).forEach(a -> {
            sb.append(a.getAttendanceId()).append(',')
                    .append(escapeCsv(a.getUserName())).append(',')
                    .append(escapeCsv(a.getCourseTitle())).append(',')
                    .append(a.getAttendanceDate()).append(',')
                    .append(a.getStatus())
                    .append('\n');
        });

        String filename = "attendance-range-" + (start == null ? "start" : start) + "-" + (end == null ? "end" : end) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export-summary")
    public ResponseEntity<byte[]> exportSummaryCsv(@RequestParam(required = false) Long courseId,
                                                    @RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate,
                                                    HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate start = (startDate == null || startDate.isBlank()) ? null : LocalDate.parse(startDate);
        LocalDate end = (endDate == null || endDate.isBlank()) ? null : LocalDate.parse(endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("enrollmentId,studentName,courseTitle,presentCount,totalCount,attendanceRate\n");
        attendanceService.getStudentSummaryByCourse(loginUser, courseId, start, end).forEach(r -> {
            sb.append(r.enrollmentId()).append(',')
                    .append(escapeCsv(r.studentName())).append(',')
                    .append(escapeCsv(r.courseTitle())).append(',')
                    .append(r.presentCount()).append(',')
                    .append(r.totalCount()).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.1f", r.attendanceRate()))
                    .append('\n');
        });

        String filename = "attendance-summary-" + (start == null ? "start" : start) + "-" + (end == null ? "end" : end) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export-absent-top")
    public ResponseEntity<byte[]> exportAbsentTopCsv(@RequestParam(defaultValue = "10") int limit,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate,
                                                      HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate start = (startDate == null || startDate.isBlank()) ? null : LocalDate.parse(startDate);
        LocalDate end = (endDate == null || endDate.isBlank()) ? null : LocalDate.parse(endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("rank,studentName,courseTitle,absentCount,totalCount,absentRate\n");
        var rows = attendanceService.getTopAbsentees(loginUser, limit, start, end);
        for (int i = 0; i < rows.size(); i++) {
            var r = rows.get(i);
            sb.append(i + 1).append(',')
                    .append(escapeCsv(r.studentName())).append(',')
                    .append(escapeCsv(r.courseTitle())).append(',')
                    .append(r.absentCount()).append(',')
                    .append(r.totalCount()).append(',')
                    .append(String.format(java.util.Locale.ROOT, "%.1f", r.absentRate()))
                    .append('\n');
        }

        String filename = "absent-top-" + (start == null ? "start" : start) + "-" + (end == null ? "end" : end) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/bulk-by-student")
    public String bulkByStudent(@RequestParam String attendanceDate,
                                @RequestParam Map<String, String> params,
                                HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        Map<Long, String> statusByEnrollmentId = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("status_")) continue;
            Long enrollmentId = Long.parseLong(key.substring("status_".length()));
            statusByEnrollmentId.put(enrollmentId, entry.getValue());
        }

        int affected = attendanceService.upsertBulkByEntries(loginUser, LocalDate.parse(attendanceDate), statusByEnrollmentId);
        return "redirect:/attendance?message=" + affected + "+건+학생별+일괄+저장되었습니다";
    }

    @PostMapping("/update")
    public String update(@RequestParam Long attendanceId,
                         @RequestParam String status,
                         HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        attendanceService.updateStatus(loginUser, attendanceId, status);
        return "redirect:/attendance?message=출석+상태가+수정되었습니다";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long attendanceId, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);

        attendanceService.deleteAttendance(loginUser, attendanceId);
        return "redirect:/attendance?message=출석+기록이+삭제되었습니다";
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        String escaped = input.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public record MonthlyIssueRow(String studentName, String courseTitle, int absentCount, int lateCount) {}
    public record MonthlyCompareRow(String studentName, String courseTitle,
                                    Map<String, Integer> absentByMonth,
                                    Map<String, Integer> lateByMonth) {}
}

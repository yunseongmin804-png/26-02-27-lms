package com.example.lms.api;

import com.example.lms.api.dto.ApiResponse;
import com.example.lms.api.dto.AttendanceResponse;
import com.example.lms.api.dto.AttendanceSummaryResponse;
import com.example.lms.auth.LoginUser;
import com.example.lms.common.AuthUtil;
import com.example.lms.service.attendance.AttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceApiController {

    private final AttendanceService attendanceService;

    public AttendanceApiController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ApiResponse<List<AttendanceResponse>> list(@RequestParam(required = false) Long courseId,
                                                      @RequestParam(required = false) String date,
                                                      HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        LocalDate parsedDate = (date == null || date.isBlank()) ? null : LocalDate.parse(date);
        List<AttendanceResponse> data = attendanceService.getAttendance(loginUser, courseId, parsedDate).stream()
                .map(v -> new AttendanceResponse(
                        v.getAttendanceId(),
                        v.getEnrollmentId(),
                        v.getUserId(),
                        v.getUserName(),
                        v.getCourseTitle(),
                        v.getAttendanceDate(),
                        v.getStatus()
                ))
                .toList();
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<Void> mark(@RequestParam Long enrollmentId,
                                  @RequestParam String attendanceDate,
                                  @RequestParam String status,
                                  HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        attendanceService.markAttendance(loginUser, enrollmentId, LocalDate.parse(attendanceDate), status);
        return ApiResponse.ok(null);
    }

    @PostMapping("/bulk")
    public ApiResponse<Integer> bulk(@RequestParam Long courseId,
                                     @RequestParam String attendanceDate,
                                     @RequestParam String status,
                                     HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        int created = attendanceService.markBulkByCourse(loginUser, courseId, LocalDate.parse(attendanceDate), status);
        return ApiResponse.ok(created);
    }

    @PostMapping("/bulk-by-student")
    public ApiResponse<Integer> bulkByStudent(@RequestParam String attendanceDate,
                                              @RequestParam java.util.Map<String, String> params,
                                              HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        java.util.Map<Long, String> statusByEnrollmentId = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("status_")) continue;
            Long enrollmentId = Long.parseLong(key.substring("status_".length()));
            statusByEnrollmentId.put(enrollmentId, entry.getValue());
        }
        int affected = attendanceService.upsertBulkByEntries(loginUser, LocalDate.parse(attendanceDate), statusByEnrollmentId);
        return ApiResponse.ok(affected);
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestParam Long attendanceId,
                                    @RequestParam String status,
                                    HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        attendanceService.updateStatus(loginUser, attendanceId, status);
        return ApiResponse.ok(null);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam Long attendanceId, HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        attendanceService.deleteAttendance(loginUser, attendanceId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/summary")
    public ApiResponse<AttendanceSummaryResponse> summary(HttpSession session) {
        LoginUser loginUser = AuthUtil.loginUser(session);
        AttendanceService.AttendanceSummary summary = attendanceService.getSummary(loginUser);
        return ApiResponse.ok(new AttendanceSummaryResponse(
                summary.presentCount(),
                summary.totalCount(),
                summary.attendanceRate()
        ));
    }
}

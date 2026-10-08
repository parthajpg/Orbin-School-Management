package com.orbin.school.attendance.controller;

import com.orbin.school.attendance.dto.AttendanceRecordDto;
import com.orbin.school.attendance.dto.AttendanceStatsDto;
import com.orbin.school.attendance.dto.MarkAttendanceRequest;
import com.orbin.school.attendance.service.AttendanceService;
import com.orbin.school.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    @PreAuthorize("hasAuthority('attendance.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> markAttendance(
            @Valid @RequestBody MarkAttendanceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.markAttendance(req)));
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAuthority('attendance.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getSectionAttendance(
            @PathVariable Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendanceBySectionAndDate(sectionId, date)));
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("hasAuthority('attendance.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceStatsDto>> getStudentStats(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getStudentStats(studentId, from, to)));
    }

    @GetMapping("/section/{sectionId}/absent")
    @PreAuthorize("hasAuthority('attendance.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceRecordDto>>> getAbsentStudents(
            @PathVariable Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAbsentStudents(sectionId, date)));
    }
}

package com.orbin.school.academic.controller;

import com.orbin.school.academic.dto.*;
import com.orbin.school.academic.service.TimetableService;
import com.orbin.school.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<PeriodSlotDto>>> getPeriodSlots() {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getPeriodSlots()));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetableForSection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getTimetableForSection(sectionId)));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getTimetableForTeacher(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Integer dayOfWeek) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getTimetableForTeacher(teacherId, dayOfWeek)));
    }

    @GetMapping("/my-schedule")
    public ResponseEntity<ApiResponse<List<TimetableEntryDto>>> getMyScheduleToday() {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.getMyScheduleToday()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN') or hasRole('PRINCIPAL') or hasRole('VICE_PRINCIPAL')")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> createTimetableEntry(
            @Valid @RequestBody CreateTimetableEntryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(timetableService.createTimetableEntry(req)));
    }

    @PostMapping("/validate-import")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN') or hasRole('PRINCIPAL') or hasRole('VICE_PRINCIPAL')")
    public ResponseEntity<ApiResponse<TimetableValidationResult>> validateImport(
            @RequestBody List<TimetableImportRow> rows) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.processBulkImport(rows, false)));
    }

    @PostMapping("/commit-import")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN') or hasRole('PRINCIPAL') or hasRole('VICE_PRINCIPAL')")
    public ResponseEntity<ApiResponse<TimetableValidationResult>> commitImport(
            @RequestBody List<TimetableImportRow> rows) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.processBulkImport(rows, true)));
    }

    @PostMapping("/{id}/substitute")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN') or hasRole('PRINCIPAL') or hasRole('VICE_PRINCIPAL')")
    public ResponseEntity<ApiResponse<TimetableEntryDto>> assignSubstitute(
            @PathVariable Long id,
            @Valid @RequestBody AssignSubstituteRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(timetableService.assignSubstitute(id, req.substituteTeacherId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN') or hasRole('PRINCIPAL') or hasRole('VICE_PRINCIPAL')")
    public ResponseEntity<ApiResponse<Void>> deleteTimetableEntry(@PathVariable Long id) {
        timetableService.deleteTimetableEntry(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}

package com.orbin.school.academic.controller;

import com.orbin.school.academic.dto.*;
import com.orbin.school.academic.service.AcademicService;
import com.orbin.school.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    // ─────────────────────────────────────────────────────────────────────────
    // Academic Years
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/years")
    @PreAuthorize("hasAuthority('academic.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<AcademicYearDto>>> getAcademicYears() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getAcademicYears()));
    }

    @PostMapping("/years")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<AcademicYearDto>> createAcademicYear(@Valid @RequestBody CreateAcademicYearRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(academicService.createAcademicYear(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classes
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/classes")
    @PreAuthorize("hasAuthority('academic.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<SchoolClassDto>>> getClasses(@RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getClasses(academicYearId)));
    }

    @PostMapping("/classes")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<SchoolClassDto>> createClass(@Valid @RequestBody CreateClassRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(academicService.createClass(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping(value = {"/sections", "/classes/{classId}/sections"})
    @PreAuthorize("hasAuthority('academic.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<SectionDto>>> getSections(@PathVariable(required = false) Long classId, @RequestParam(required = false) Long queryClassId) {
        Long targetClassId = classId != null ? classId : queryClassId;
        return ResponseEntity.ok(ApiResponse.ok(academicService.getSections(targetClassId)));
    }

    @PostMapping("/sections")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<SectionDto>> createSection(@Valid @RequestBody CreateSectionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(academicService.createSection(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subjects
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/subjects")
    @PreAuthorize("hasAuthority('academic.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getSubjects(@RequestParam(required = false) Long classId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getSubjects(classId)));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<SubjectDto>> createSubject(@Valid @RequestBody CreateSubjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(academicService.createSubject(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Teacher Assignments
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/teacher-assignments")
    @PreAuthorize("hasAuthority('academic.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<TeacherAssignmentDto>>> getTeacherAssignments(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long teacherId) {
        return ResponseEntity.ok(ApiResponse.ok(academicService.getTeacherAssignments(academicYearId, teacherId)));
    }

    @PostMapping("/teacher-assignments")
    @PreAuthorize("hasAuthority('academic.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<TeacherAssignmentDto>> assignTeacher(@Valid @RequestBody AssignTeacherRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(academicService.assignTeacher(req)));
    }
}

package com.orbin.school.syllabus.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.syllabus.dto.*;
import com.orbin.school.syllabus.service.SyllabusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/syllabus")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService syllabusService;

    // ─────────────────────────────────────────────────────────────────────────
    // Terms
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/terms")
    @PreAuthorize("hasAuthority('syllabus.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<TermDto>>> getTerms(
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.getTerms(academicYearId)));
    }

    @PostMapping("/terms")
    @PreAuthorize("hasAuthority('syllabus.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<TermDto>> createTerm(
            @Valid @RequestBody CreateTermRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(syllabusService.createTerm(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chapters
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/chapters")
    @PreAuthorize("hasAuthority('syllabus.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<ChapterDto>>> getChapters(
            @RequestParam Long subjectId,
            @RequestParam(required = false) Long termId) {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.getChapters(subjectId, termId)));
    }

    @PostMapping("/chapters")
    @PreAuthorize("hasAuthority('syllabus.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<ChapterDto>> createChapter(
            @Valid @RequestBody CreateChapterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(syllabusService.createChapter(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Progress
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/progress")
    @PreAuthorize("hasAuthority('syllabus.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<SyllabusProgressDto>> updateProgress(
            @Valid @RequestBody UpdateProgressRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.updateProgress(req)));
    }

    @GetMapping("/progress/section/{sectionId}")
    @PreAuthorize("hasAuthority('syllabus.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<SyllabusProgressDto>>> getSectionProgress(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(syllabusService.getSectionProgress(sectionId)));
    }
}

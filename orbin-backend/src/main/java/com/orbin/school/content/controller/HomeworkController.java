package com.orbin.school.content.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.content.dto.*;
import com.orbin.school.content.service.HomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkService homeworkService;

    // ─────────────────────────────────────────────────────────────────────────
    // Homework
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/homework")
    @PreAuthorize("hasAuthority('homework.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<HomeworkDto>> createHomework(@Valid @RequestBody CreateHomeworkRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(homeworkService.createHomework(req)));
    }

    @GetMapping("/homework/section/{sectionId}")
    @PreAuthorize("hasAuthority('homework.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<Page<HomeworkDto>>> getHomework(
            @PathVariable Long sectionId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(homeworkService.getHomework(sectionId, pageable)));
    }

    @GetMapping("/homework/section/{sectionId}/today")
    @PreAuthorize("hasAuthority('homework.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<HomeworkDto>>> getTodayHomework(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(homeworkService.getTodayHomework(sectionId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Materials
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/materials")
    @PreAuthorize("hasAuthority('materials.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<MaterialDto>> createMaterial(@Valid @RequestBody CreateMaterialRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(homeworkService.createMaterial(req)));
    }

    @GetMapping("/materials/subject/{subjectId}")
    @PreAuthorize("hasAuthority('materials.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<Page<MaterialDto>>> getMaterials(
            @PathVariable Long subjectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(homeworkService.getMaterials(subjectId, pageable)));
    }
}

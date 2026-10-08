package com.orbin.school.exam.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.exam.dto.*;
import com.orbin.school.exam.service.ExamService;
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
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @PreAuthorize("hasAuthority('tests.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<TestDto>> createTest(@Valid @RequestBody CreateTestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(examService.createTest(req)));
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAuthority('tests.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TestDto>>> getTests(
            @PathVariable Long sectionId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(examService.getTests(sectionId, pageable)));
    }

    @PostMapping("/marks")
    @PreAuthorize("hasAuthority('results.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> enterMarks(@Valid @RequestBody EnterMarksRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(examService.enterMarks(req)));
    }

    @GetMapping("/{testId}/results")
    @PreAuthorize("hasAuthority('results.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> getTestResults(@PathVariable Long testId) {
        return ResponseEntity.ok(ApiResponse.ok(examService.getTestResults(testId)));
    }

    @GetMapping("/student/{studentId}/report-card")
    @PreAuthorize("hasAuthority('results.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultDto>>> getStudentReportCard(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(examService.getStudentReportCard(studentId)));
    }
}

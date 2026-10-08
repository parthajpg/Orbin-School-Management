package com.orbin.school.fee.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.fee.dto.*;
import com.orbin.school.fee.service.FeeService;
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
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    // ─────────────────────────────────────────────────────────────────────────
    // Structures
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/structures")
    @PreAuthorize("hasAuthority('fees.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeStructureDto>>> getFeeStructures(
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getFeeStructures(academicYearId)));
    }

    @PostMapping("/structures")
    @PreAuthorize("hasAuthority('fees.write') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<FeeStructureDto>> createFeeStructure(
            @Valid @RequestBody CreateFeeStructureRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(feeService.createFeeStructure(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Assign
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/assign")
    @PreAuthorize("hasAuthority('fees.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentFeeDto>>> assignFee(
            @Valid @RequestBody AssignFeeRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.assignFee(req)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Student fees & payments
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('fees.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentFeeDto>>> getAllStudentFees() {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getAllStudentFees()));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('fees.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<List<StudentFeeDto>>> getStudentFees(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getStudentFees(studentId)));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('payments.record') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentDto>> recordPayment(
            @Valid @RequestBody RecordPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(feeService.recordPayment(req)));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAuthority('fees.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getPayments(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getPayments(pageable)));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('fees.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<FeeDashboardDto>> getFeeDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getFeeDashboard()));
    }
}

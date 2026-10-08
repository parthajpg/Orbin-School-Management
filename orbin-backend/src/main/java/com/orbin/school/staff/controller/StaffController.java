package com.orbin.school.staff.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.staff.dto.CreateStaffRequest;
import com.orbin.school.staff.dto.StaffDto;
import com.orbin.school.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffDto>>> getAllStaff() {
        return ResponseEntity.ok(ApiResponse.ok(staffService.getAllStaff()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> getStaffById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.getStaffById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StaffDto>> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffDto response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffDto>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody CreateStaffRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.updateStaff(id, request)));
    }

    @PostMapping("/{id}/reset-credentials")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetCredentials(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(staffService.resetCredentials(id)));
    }
}

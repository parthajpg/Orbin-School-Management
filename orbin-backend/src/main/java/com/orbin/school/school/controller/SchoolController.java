package com.orbin.school.school.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.school.dto.SchoolDto;
import com.orbin.school.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolDto>>> getAllSchools() {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getAllActiveSchools()));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SchoolDto>> getCurrentSchool() {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getCurrentSchool()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolDto>> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getSchoolById(id)));
    }
}

package com.orbin.school.student.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.student.dto.CreateStudentRequest;
import com.orbin.school.student.dto.StudentResponse;
import com.orbin.school.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> create(
            @Valid @RequestBody CreateStudentRequest request,
            @RequestParam(required = false) Long academicYearId) {
        StudentResponse response = studentService.createStudent(request, academicYearId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<java.util.List<StudentResponse>>> bulkCreate(
            @Valid @RequestBody java.util.List<CreateStudentRequest> requests,
            @RequestParam(required = false) Long academicYearId) {
        java.util.List<StudentResponse> response = studentService.bulkCreateStudents(requests, academicYearId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getStudent(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> list(
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 25) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                studentService.listStudents(academicYearId, query, pageable)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.updateStudent(id, request)));
    }
}

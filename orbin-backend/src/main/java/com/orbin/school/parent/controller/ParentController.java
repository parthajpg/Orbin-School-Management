package com.orbin.school.parent.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.parent.dto.CreateParentRequest;
import com.orbin.school.parent.dto.LinkStudentRequest;
import com.orbin.school.parent.dto.ParentResponse;
import com.orbin.school.parent.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    @PreAuthorize("hasAuthority('parents.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<Page<ParentResponse>>> searchParents(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.searchParents(q, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('parents.read') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<ParentResponse>> getParentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getParentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('parents.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<ParentResponse>> createParent(@Valid @RequestBody CreateParentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(parentService.createParent(req)));
    }

    @PostMapping("/{id}/students")
    @PreAuthorize("hasAuthority('parents.manage') or hasRole('ORBIN_ADMIN')")
    public ResponseEntity<ApiResponse<ParentResponse>> linkStudent(
            @PathVariable Long id,
            @Valid @RequestBody LinkStudentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.linkStudent(id, req)));
    }
}

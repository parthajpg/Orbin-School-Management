package com.orbin.school.parent.dto;

import java.time.Instant;
import java.util.List;

public record ParentResponse(
    Long id,
    String firstName,
    String lastName,
    String fullName,
    String phone,
    String normalizedPhone,
    String email,
    String occupation,
    String address,
    boolean whatsappVerified,
    List<LinkedStudentDto> students,
    Instant createdAt
) {
    public record LinkedStudentDto(
        Long studentId,
        String admissionNumber,
        String studentName,
        String relationship,
        boolean primary
    ) {}
}

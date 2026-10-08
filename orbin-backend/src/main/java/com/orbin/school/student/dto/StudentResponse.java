package com.orbin.school.student.dto;

import com.orbin.school.student.entity.Student;

import java.time.LocalDate;
import java.time.Instant;

public record StudentResponse(
    Long id,
    String admissionNumber,
    String firstName,
    String lastName,
    String fullName,
    LocalDate dateOfBirth,
    String gender,
    Long sectionId,
    String sectionName,
    String className,
    LocalDate admissionDate,
    String status,
    String address,
    String profileImageUrl,
    Instant createdAt
) {
    public static StudentResponse from(Student s) {
        return new StudentResponse(
            s.getId(),
            s.getAdmissionNumber(),
            s.getFirstName(),
            s.getLastName(),
            s.getFullName(),
            s.getDateOfBirth(),
            s.getGender(),
            s.getSection().getId(),
            s.getSection().getName(),
            s.getSection().getSchoolClass().getName(),
            s.getAdmissionDate(),
            s.getStatus().name(),
            s.getAddress(),
            s.getProfileImageUrl(),
            s.getCreatedAt()
        );
    }
}

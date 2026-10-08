package com.orbin.school.student.dto;

import com.orbin.school.student.entity.Student;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateStudentRequest(
    @NotBlank @Size(max = 30) String admissionNumber,
    @NotBlank @Size(max = 80) String firstName,
    @Size(max = 80) String lastName,
    LocalDate dateOfBirth,
    @Pattern(regexp = "MALE|FEMALE|OTHER") String gender,
    @NotNull Long sectionId,
    @NotNull LocalDate admissionDate,
    String address
) {}

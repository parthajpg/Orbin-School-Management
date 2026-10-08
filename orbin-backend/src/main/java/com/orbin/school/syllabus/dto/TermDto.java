package com.orbin.school.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TermDto(
    Long id,
    Long academicYearId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    Integer displayOrder
) {}

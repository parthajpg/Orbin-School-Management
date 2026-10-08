package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SchoolClassDto(
    Long id,
    Long academicYearId,
    String name,
    Integer displayOrder
) {}

package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateClassRequest(
    @NotNull Long academicYearId,
    @NotBlank String name,
    Integer displayOrder
) {}

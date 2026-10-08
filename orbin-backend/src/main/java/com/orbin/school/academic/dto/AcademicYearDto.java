package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AcademicYearDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    boolean current,
    String status
) {}

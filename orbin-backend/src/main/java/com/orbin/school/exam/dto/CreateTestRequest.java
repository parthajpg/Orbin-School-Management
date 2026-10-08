package com.orbin.school.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTestRequest(
    @NotNull Long sectionId,
    @NotNull Long subjectId,
    @NotBlank String title,
    LocalDate testDate,
    Integer durationMin,
    @NotNull BigDecimal maxMarks,
    String instructions
) {}

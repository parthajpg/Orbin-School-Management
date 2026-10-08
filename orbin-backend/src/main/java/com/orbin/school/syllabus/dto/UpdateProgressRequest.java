package com.orbin.school.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateProgressRequest(
    @NotNull Long chapterId,
    @NotNull Long sectionId,
    @NotBlank String status, // NOT_STARTED, IN_PROGRESS, COMPLETED, REVISION
    LocalDate plannedStartDate,
    LocalDate plannedCompletionDate,
    LocalDate actualCompletionDate,
    Integer estimatedPeriods,
    Integer actualPeriods,
    String notes,
    LocalDate revisionDate
) {}

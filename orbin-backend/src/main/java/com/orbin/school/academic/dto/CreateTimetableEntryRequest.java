package com.orbin.school.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateTimetableEntryRequest(
    Long academicYearId,
    @NotNull(message = "Section ID is required")
    Long sectionId,
    @NotNull(message = "Period slot ID is required")
    Long periodSlotId,
    @NotNull(message = "Day of week is required")
    @Min(1) @Max(7)
    Integer dayOfWeek,
    @NotNull(message = "Teacher ID is required")
    Long teacherId,
    Long subjectId, // Optional for Nursery / Homeroom classes
    String roomNumber
) {}

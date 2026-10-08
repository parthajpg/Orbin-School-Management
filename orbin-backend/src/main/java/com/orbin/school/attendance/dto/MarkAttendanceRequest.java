package com.orbin.school.attendance.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record MarkAttendanceRequest(
    @NotNull Long sectionId,
    @NotNull LocalDate date,
    @NotNull List<StudentAttendanceEntry> entries
) {
    public record StudentAttendanceEntry(
        @NotNull Long studentId,
        @NotNull String status, // PRESENT, ABSENT, LATE, EXCUSED
        String notes
    ) {}
}

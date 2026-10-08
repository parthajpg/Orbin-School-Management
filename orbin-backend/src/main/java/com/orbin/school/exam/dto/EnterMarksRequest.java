package com.orbin.school.exam.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record EnterMarksRequest(
    @NotNull Long testId,
    @NotNull List<StudentMarkEntry> marks
) {
    public record StudentMarkEntry(
        @NotNull Long studentId,
        @NotNull BigDecimal marksObtained,
        String teacherNote
    ) {}
}

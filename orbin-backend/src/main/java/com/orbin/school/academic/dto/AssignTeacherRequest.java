package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTeacherRequest(
    @NotNull Long academicYearId,
    @NotNull Long teacherId,
    @NotNull Long classId,
    @NotNull Long sectionId,
    @NotNull Long subjectId
) {}

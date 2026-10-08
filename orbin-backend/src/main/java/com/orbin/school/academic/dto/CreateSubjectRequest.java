package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSubjectRequest(
    @NotNull Long classId,
    @NotBlank String name,
    String code,
    String subjectType
) {}

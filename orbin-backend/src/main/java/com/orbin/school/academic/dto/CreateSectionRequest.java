package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSectionRequest(
    @NotNull Long classId,
    @NotBlank String name,
    Integer capacity,
    Long classTeacherId
) {}

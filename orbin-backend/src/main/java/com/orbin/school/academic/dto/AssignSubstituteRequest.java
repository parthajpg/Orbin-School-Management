package com.orbin.school.academic.dto;

import jakarta.validation.constraints.NotNull;

public record AssignSubstituteRequest(
    @NotNull(message = "Substitute teacher ID is required")
    Long substituteTeacherId
) {}

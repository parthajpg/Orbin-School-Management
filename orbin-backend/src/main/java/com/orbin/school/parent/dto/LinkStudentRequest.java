package com.orbin.school.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkStudentRequest(
    @NotNull Long studentId,
    @NotBlank String relationship, // FATHER, MOTHER, GUARDIAN
    boolean primary,
    boolean canReceiveNotifications
) {}

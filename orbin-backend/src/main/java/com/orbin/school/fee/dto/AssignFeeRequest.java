package com.orbin.school.fee.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AssignFeeRequest(
    @NotNull Long feeStructureId,
    Long classId,
    Long sectionId,
    List<Long> studentIds,
    LocalDate dueDate
) {}

package com.orbin.school.fee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateFeeStructureRequest(
    @NotNull Long academicYearId,
    Long classId,
    @NotBlank String name,
    String description,
    @NotEmpty List<FeeItemEntry> items
) {
    public record FeeItemEntry(
        @NotBlank String category,
        @NotBlank String name,
        @NotNull BigDecimal amount,
        LocalDate dueDate
    ) {}
}

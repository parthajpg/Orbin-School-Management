package com.orbin.school.fee.dto;

import java.math.BigDecimal;
import java.util.List;

public record FeeStructureDto(
    Long id,
    Long academicYearId,
    String academicYearName,
    Long classId,
    String className,
    String name,
    String description,
    BigDecimal totalAmount,
    List<FeeItemDto> items
) {}

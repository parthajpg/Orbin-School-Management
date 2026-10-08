package com.orbin.school.fee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StudentFeeDto(
    Long id,
    Long studentId,
    String studentName,
    String admissionNumber,
    Long feeStructureId,
    String feeStructureName,
    Long academicYearId,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal outstanding,
    LocalDate dueDate,
    String status
) {}

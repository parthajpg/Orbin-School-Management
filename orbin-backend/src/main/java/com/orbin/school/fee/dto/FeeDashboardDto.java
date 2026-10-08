package com.orbin.school.fee.dto;

import java.math.BigDecimal;

public record FeeDashboardDto(
    BigDecimal totalExpected,
    BigDecimal totalCollected,
    BigDecimal totalOutstanding,
    long totalStudentsWithFees,
    long fullyPaidStudents,
    long overdueStudents
) {}

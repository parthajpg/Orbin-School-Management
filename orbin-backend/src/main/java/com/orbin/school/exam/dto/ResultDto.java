package com.orbin.school.exam.dto;

import java.math.BigDecimal;

public record ResultDto(
    Long id,
    Long testId,
    String testTitle,
    Long studentId,
    String studentName,
    String admissionNumber,
    BigDecimal marksObtained,
    BigDecimal maxMarks,
    BigDecimal percentage,
    String teacherNote
) {}

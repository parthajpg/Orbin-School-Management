package com.orbin.school.exam.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TestDto(
    Long id,
    Long sectionId,
    String sectionName,
    Long subjectId,
    String subjectName,
    String teacherName,
    String title,
    LocalDate testDate,
    Integer durationMin,
    BigDecimal maxMarks,
    String instructions,
    String status
) {}

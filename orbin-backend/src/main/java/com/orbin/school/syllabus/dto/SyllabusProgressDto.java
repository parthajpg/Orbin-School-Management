package com.orbin.school.syllabus.dto;

import java.time.LocalDate;

public record SyllabusProgressDto(
    Long id,
    Long chapterId,
    String chapterTitle,
    Long sectionId,
    String sectionName,
    String teacherName,
    String status,
    LocalDate plannedStartDate,
    LocalDate plannedCompletionDate,
    LocalDate actualCompletionDate,
    Integer estimatedPeriods,
    Integer actualPeriods,
    String notes,
    LocalDate revisionDate
) {}

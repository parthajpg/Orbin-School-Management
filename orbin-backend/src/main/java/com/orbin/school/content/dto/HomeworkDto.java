package com.orbin.school.content.dto;

import java.time.LocalDate;
import java.time.Instant;

public record HomeworkDto(
    Long id,
    Long sectionId,
    String sectionName,
    Long subjectId,
    String subjectName,
    String teacherName,
    String title,
    String description,
    LocalDate dueDate,
    String attachmentUrl,
    String status,
    Instant createdAt
) {}

package com.orbin.school.content.dto;

import java.time.Instant;

public record MaterialDto(
    Long id,
    Long subjectId,
    String subjectName,
    Long chapterId,
    String chapterTitle,
    String teacherName,
    String title,
    String description,
    String materialType,
    String fileUrl,
    String externalUrl,
    Instant createdAt
) {}

package com.orbin.school.syllabus.dto;

import java.util.List;

public record ChapterDto(
    Long id,
    Long subjectId,
    String subjectName,
    Long termId,
    String termName,
    String title,
    String description,
    Integer displayOrder,
    List<TopicDto> topics
) {}

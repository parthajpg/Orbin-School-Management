package com.orbin.school.syllabus.dto;

public record TopicDto(
    Long id,
    Long chapterId,
    String title,
    Integer displayOrder
) {}

package com.orbin.school.academic.dto;

public record SectionDto(
    Long id,
    Long classId,
    String className,
    String name,
    Integer capacity,
    Long classTeacherId,
    String classTeacherName
) {}

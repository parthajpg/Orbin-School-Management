package com.orbin.school.academic.dto;

public record SubjectDto(
    Long id,
    Long classId,
    String name,
    String code,
    String subjectType
) {}

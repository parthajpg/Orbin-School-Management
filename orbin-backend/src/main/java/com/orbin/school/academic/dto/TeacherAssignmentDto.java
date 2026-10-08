package com.orbin.school.academic.dto;

public record TeacherAssignmentDto(
    Long id,
    Long academicYearId,
    Long teacherId,
    String teacherName,
    Long classId,
    String className,
    Long sectionId,
    String sectionName,
    Long subjectId,
    String subjectName
) {}

package com.orbin.school.academic.dto;

import java.time.LocalTime;

public record TimetableEntryDto(
    Long id,
    Long academicYearId,
    Long sectionId,
    String className,
    String sectionName,
    Long periodSlotId,
    Integer slotNumber,
    String slotName,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isBreak,
    Integer dayOfWeek,
    String dayName,
    Long teacherId,
    String teacherName,
    String teacherEmail,
    Long subjectId,
    String subjectName,
    String subjectCode,
    String roomNumber,
    Boolean isSubstitution,
    Long originalTeacherId,
    String originalTeacherName
) {}

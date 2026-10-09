package com.orbin.school.academic.dto;

import java.util.List;

public record TimetableImportRow(
    String className,
    String sectionName,
    String day,          // "Monday", "Tuesday", etc. or "1".."6"
    Integer periodNumber, // 1, 2, 3, etc.
    String subjectCode,  // optional for Nursery/Primary
    String teacherEmail, // teacher's email or employee ID
    String roomNumber
) {}

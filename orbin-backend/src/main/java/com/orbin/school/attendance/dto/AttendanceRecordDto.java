package com.orbin.school.attendance.dto;

import java.time.LocalDate;

public record AttendanceRecordDto(
    Long id,
    Long studentId,
    String studentName,
    String admissionNumber,
    Long sectionId,
    LocalDate date,
    String status,
    String notes,
    String markedByName
) {}

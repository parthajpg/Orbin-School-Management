package com.orbin.school.attendance.dto;

import java.time.LocalDate;

public record AttendanceStatsDto(
    Long studentId,
    LocalDate from,
    LocalDate to,
    long presentDays,
    long totalDays,
    double percentage
) {}

package com.orbin.school.academic.dto;

import java.time.LocalTime;

public record PeriodSlotDto(
    Long id,
    Integer slotNumber,
    String name,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isBreak,
    String tier
) {}

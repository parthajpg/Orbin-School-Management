package com.orbin.school.fee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeItemDto(
    Long id,
    String category,
    String name,
    BigDecimal amount,
    LocalDate dueDate
) {}

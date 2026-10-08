package com.orbin.school.fee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public record PaymentDto(
    Long id,
    Long studentFeeId,
    Long studentId,
    String studentName,
    String admissionNumber,
    BigDecimal amount,
    LocalDate paymentDate,
    String paymentMethod,
    String referenceNumber,
    String receiptNumber,
    String notes,
    String recordedByName,
    Instant createdAt
) {}

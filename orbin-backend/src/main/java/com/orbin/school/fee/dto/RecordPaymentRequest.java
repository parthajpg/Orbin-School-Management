package com.orbin.school.fee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordPaymentRequest(
    @NotNull Long studentFeeId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    LocalDate paymentDate,
    @NotBlank String paymentMethod, // CASH, UPI, CHEQUE, ONLINE
    String referenceNumber,
    String notes
) {}

package com.reese.transactiontrackingapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
    @NotNull(message = "Date is required")
    LocalDate date,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "-999999999.99", message = "Amount is too small")
    BigDecimal amount,

    @NotBlank(message = "Category is required")
    String category
) {
}

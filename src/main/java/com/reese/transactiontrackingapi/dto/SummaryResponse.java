package com.reese.transactiontrackingapi.dto;

import java.math.BigDecimal;

public record SummaryResponse(
    BigDecimal total,
    long transactionCount,
    BigDecimal incomeTotal,
    BigDecimal expenseTotal,
    String topCategory,
    String summaryText
) {
}

package com.reese.transactiontrackingapi.dto;

import java.math.BigDecimal;

public record CategorySpending(
    String category,
    BigDecimal amount
) {
}

package com.reese.transactiontrackingapi.dto;

public record ImportResponse(
    String message,
    int rowsImported
) {
}

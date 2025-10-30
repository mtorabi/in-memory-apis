package com.reckless_bank.in_memory_apis.account.application.dto;

import java.math.BigDecimal;

public record TransactionRequest(BigDecimal amount) {
    public TransactionRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

}

package com.reckless_bank.in_memory_apis.account.application.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(String accountHolder, BigDecimal initialBalance) {
    public CreateAccountRequest {
        if (accountHolder == null || accountHolder.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder cannot be null or empty");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be null or negative");
        }
    }
}

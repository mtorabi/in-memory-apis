package com.reckless_bank.in_memory_apis.account.application.dto;

import java.math.BigDecimal;

public record TransferRequest(String fromAccountId, String toAccountId, BigDecimal amount) {
    public TransferRequest {
        // At least one account must be specified
        if ((fromAccountId == null || fromAccountId.trim().isEmpty()) && 
            (toAccountId == null || toAccountId.trim().isEmpty())) {
            throw new IllegalArgumentException("At least one account ID must be specified");
        }
        
        // Cannot transfer to the same account (only if both are non-null and non-empty)
        if (fromAccountId != null && !fromAccountId.trim().isEmpty() && 
            toAccountId != null && !toAccountId.trim().isEmpty() && 
            fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }
}
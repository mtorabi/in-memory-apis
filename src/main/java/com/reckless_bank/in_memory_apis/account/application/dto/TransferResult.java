
package com.reckless_bank.in_memory_apis.account.application.dto;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;

public record TransferResult(Account fromAccount, Account toAccount) {
    public TransferResult {
        // At least one account must be provided
        if (fromAccount == null && toAccount == null) {
            throw new IllegalArgumentException("At least one account must be provided in the result");
        }
    }
}
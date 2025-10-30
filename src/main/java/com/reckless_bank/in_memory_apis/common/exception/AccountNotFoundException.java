package com.reckless_bank.in_memory_apis.common.exception;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends RuntimeException {
    
    private final String accountId;
    
    public AccountNotFoundException(String accountId) {
        super(String.format("Account not found with ID: %s", accountId));
        this.accountId = accountId;
    }
    
    public AccountNotFoundException(String accountId, String message) {
        super(message);
        this.accountId = accountId;
    }
    
    public String getAccountId() {
        return accountId;
    }
}
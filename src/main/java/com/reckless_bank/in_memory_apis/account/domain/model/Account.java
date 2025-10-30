package com.reckless_bank.in_memory_apis.account.domain.model;

import java.math.BigDecimal;

public record Account(String accountId, String accountHolder, BigDecimal balance) {
    
    public Account {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty");
        }
        if (accountHolder == null || accountHolder.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder cannot be null or empty");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }
    
    // Factory method for creating new accounts with zero balance
    public static Account newAccount(String accountId, String accountHolder) {
        return new Account(accountId, accountHolder, BigDecimal.ZERO);
    }
    
    // Method to deposit money (returns new Account instance)
    public Account deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        return new Account(accountId, accountHolder, balance.add(amount));
    }
    
    // Method to withdraw money (returns new Account instance)
    public Account withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        return new Account(accountId, accountHolder, balance.subtract(amount));
    }
    
    // Utility method to check if account has sufficient funds
    public boolean hasSufficientFunds(BigDecimal amount) {
        return amount != null && balance.compareTo(amount) >= 0;
    }
    
    // Utility method to check if account is active (has positive balance)
    public boolean isActive() {
        return balance.compareTo(BigDecimal.ZERO) > 0;
    }
}

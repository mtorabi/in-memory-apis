package com.reckless_bank.in_memory_apis.common.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when an invalid transaction is attempted
 */
public class InvalidTransactionException extends RuntimeException {
    
    private final String transactionType;
    private final BigDecimal amount;
    private final String details;
    
    public InvalidTransactionException(String transactionType, BigDecimal amount, String details) {
        super(String.format("Invalid %s transaction. Amount: %s. Details: %s", 
              transactionType, amount, details));
        this.transactionType = transactionType;
        this.amount = amount;
        this.details = details;
    }
    
    public InvalidTransactionException(String message) {
        super(message);
        this.transactionType = "unknown";
        this.amount = null;
        this.details = message;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getDetails() {
        return details;
    }
}
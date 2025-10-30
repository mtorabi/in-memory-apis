package com.reckless_bank.in_memory_apis.common.exception;

/**
 * Exception thrown when there are repository or data access issues
 */
public class RepositoryException extends RuntimeException {
    
    private final String operation;
    private final String entityType;
    private final String entityId;
    
    public RepositoryException(String operation, String entityType, String entityId, String message) {
        super(String.format("Repository error during %s operation for %s with ID %s: %s", 
              operation, entityType, entityId, message));
        this.operation = operation;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    public RepositoryException(String operation, String entityType, String entityId, Throwable cause) {
        super(String.format("Repository error during %s operation for %s with ID %s", 
              operation, entityType, entityId), cause);
        this.operation = operation;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
}
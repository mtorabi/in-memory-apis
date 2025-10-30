package com.reckless_bank.in_memory_apis.common.exception;

import com.reckless_bank.in_memory_apis.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(
            AccountNotFoundException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Account not found - TraceId: {}, AccountId: {}", traceId, ex.getAccountId());
        
        Map<String, Object> details = new HashMap<>();
        details.put("accountId", ex.getAccountId());
        details.put("suggestion", "Please verify the account ID and try again");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Account Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(
            InsufficientFundsException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Insufficient funds - TraceId: {}, AccountId: {}, Requested: {}, Available: {}", 
                   traceId, ex.getAccountId(), ex.getRequestedAmount(), ex.getAvailableBalance());
        
        Map<String, Object> details = new HashMap<>();
        details.put("accountId", ex.getAccountId());
        details.put("requestedAmount", ex.getRequestedAmount());
        details.put("availableBalance", ex.getAvailableBalance());
        details.put("shortfall", ex.getRequestedAmount().subtract(ex.getAvailableBalance()));
        details.put("suggestion", "Please reduce the transaction amount or deposit additional funds");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Insufficient Funds",
            ex.getMessage(),
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionException(
            InvalidTransactionException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Invalid transaction - TraceId: {}, Type: {}, Amount: {}, Details: {}", 
                   traceId, ex.getTransactionType(), ex.getAmount(), ex.getDetails());
        
        Map<String, Object> details = new HashMap<>();
        details.put("transactionType", ex.getTransactionType());
        if (ex.getAmount() != null) {
            details.put("amount", ex.getAmount());
        }
        details.put("details", ex.getDetails());
        details.put("suggestion", "Please check the transaction parameters and try again");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Transaction",
            ex.getMessage(),
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryException(
            RepositoryException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.error("Repository error - TraceId: {}, Operation: {}, EntityType: {}, EntityId: {}", 
                    traceId, ex.getOperation(), ex.getEntityType(), ex.getEntityId(), ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("operation", ex.getOperation());
        details.put("entityType", ex.getEntityType());
        details.put("entityId", ex.getEntityId());
        details.put("suggestion", "This is a system error. Please try again later or contact support");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Data Access Error",
            "An error occurred while accessing the data store",
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Validation error - TraceId: {}", traceId);
        
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .toList();
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Request validation failed",
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Invalid request body - TraceId: {}", traceId);
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request Body",
            "The request body is malformed or contains invalid data",
            request.getRequestURI(),
            traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Parameter type mismatch - TraceId: {}, Parameter: {}, Value: {}", 
                   traceId, ex.getName(), ex.getValue());
        
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getName());
        details.put("value", ex.getValue());
        Class<?> requiredType = ex.getRequiredType();
        details.put("expectedType", requiredType != null ? requiredType.getSimpleName() : "unknown");
        details.put("suggestion", "Please provide a valid value for the parameter");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter",
            String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()),
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.warn("Illegal argument - TraceId: {}, Message: {}", traceId, ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("suggestion", "Please check your request parameters and try again");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ex.getMessage(),
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String traceId = generateTraceId();
        logger.error("Unexpected error - TraceId: {}", traceId, ex);
        
        Map<String, Object> details = new HashMap<>();
        details.put("type", ex.getClass().getSimpleName());
        details.put("suggestion", "An unexpected error occurred. Please try again later or contact support");
        
        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred while processing your request",
            request.getRequestURI(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
        return new ErrorResponse.ValidationError(
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage()
        );
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
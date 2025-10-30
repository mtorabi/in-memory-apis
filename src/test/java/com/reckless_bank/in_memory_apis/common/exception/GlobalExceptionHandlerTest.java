package com.reckless_bank.in_memory_apis.common.exception;

import com.reckless_bank.in_memory_apis.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/accounts/test");
    }

    @Test
    void shouldHandleAccountNotFoundException() {
        // Given
        String accountId = "ACC001";
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAccountNotFoundException(exception, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.status());
        assertEquals("Account Not Found", errorResponse.error());
        assertTrue(errorResponse.message().contains(accountId));
        assertEquals("/api/accounts/test", errorResponse.path());
        assertNotNull(errorResponse.traceId());
    }

    @Test
    void shouldHandleInsufficientFundsException() {
        // Given
        String accountId = "ACC001";
        BigDecimal requestedAmount = new BigDecimal("1000.00");
        BigDecimal availableBalance = new BigDecimal("500.00");
        InsufficientFundsException exception = new InsufficientFundsException(
            accountId, requestedAmount, availableBalance);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleInsufficientFundsException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Insufficient Funds", errorResponse.error());
        assertTrue(errorResponse.message().contains(accountId));
        assertNotNull(errorResponse.details());
        
        Map<String, Object> details = errorResponse.details();
        assertEquals(accountId, details.get("accountId"));
        assertEquals(requestedAmount, details.get("requestedAmount"));
        assertEquals(availableBalance, details.get("availableBalance"));
        assertEquals(new BigDecimal("500.00"), details.get("shortfall"));
    }

    @Test
    void shouldHandleInvalidTransactionException() {
        // Given
        String transactionType = "withdrawal";
        BigDecimal amount = new BigDecimal("100.00");
        String details = "Invalid amount";
        InvalidTransactionException exception = new InvalidTransactionException(
            transactionType, amount, details);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleInvalidTransactionException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Invalid Transaction", errorResponse.error());
        assertNotNull(errorResponse.details());
        
        Map<String, Object> responseDetails = errorResponse.details();
        assertEquals(transactionType, responseDetails.get("transactionType"));
        assertEquals(amount, responseDetails.get("amount"));
        assertEquals(details, responseDetails.get("details"));
    }

    @Test
    void shouldHandleRepositoryException() {
        // Given
        String operation = "save";
        String entityType = "Account";
        String entityId = "ACC001";
        RepositoryException exception = new RepositoryException(
            operation, entityType, entityId, "Database connection failed");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleRepositoryException(exception, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.status());
        assertEquals("Data Access Error", errorResponse.error());
        assertEquals("An error occurred while accessing the data store", errorResponse.message());
        assertNotNull(errorResponse.details());
        
        Map<String, Object> details = errorResponse.details();
        assertEquals(operation, details.get("operation"));
        assertEquals(entityType, details.get("entityType"));
        assertEquals(entityId, details.get("entityId"));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        FieldError fieldError = new FieldError("transactionRequest", "amount", 
            new BigDecimal("-100"), false, null, null, "Amount must be positive");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationException(methodArgumentNotValidException, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Validation Failed", errorResponse.error());
        assertEquals("Request validation failed", errorResponse.message());
        assertNotNull(errorResponse.validationErrors());
        assertEquals(1, errorResponse.validationErrors().size());
        
        ErrorResponse.ValidationError validationError = errorResponse.validationErrors().get(0);
        assertEquals("amount", validationError.field());
        assertEquals("Amount must be positive", validationError.message());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input parameter");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleIllegalArgumentException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.status());
        assertEquals("Invalid Argument", errorResponse.error());
        assertEquals("Invalid input parameter", errorResponse.message());
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGenericException(exception, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.status());
        assertEquals("Internal Server Error", errorResponse.error());
        assertEquals("An unexpected error occurred while processing your request", errorResponse.message());
        assertNotNull(errorResponse.details());
        
        Map<String, Object> details = errorResponse.details();
        assertEquals("RuntimeException", details.get("type"));
    }
}
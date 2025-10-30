package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepositUseCaseTest {

    @Mock
    private TransferUseCase transferUseCase;

    @InjectMocks
    private DepositUseCase depositUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldDepositSuccessfully() {
        // Given
        String accountId = "ACC001";
        BigDecimal depositAmount = new BigDecimal("150.00");
        TransactionRequest request = new TransactionRequest(depositAmount);
        
        Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("650.00"));
        TransferResult transferResult = new TransferResult(null, updatedAccount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenReturn(Optional.of(transferResult));

        // When
        Optional<Account> result = depositUseCase.execute(accountId, request);

        // Then
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("650.00"), result.get().balance());
        assertEquals(accountId, result.get().accountId());
        
        // Verify that TransferUseCase was called with correct parameters
        verify(transferUseCase, times(1)).execute(argThat(transferRequest -> 
            transferRequest.fromAccountId() == null &&
            transferRequest.toAccountId().equals(accountId) &&
            transferRequest.amount().equals(depositAmount)
        ));
    }

    @Test
    void shouldReturnEmptyWhenTransferFails() {
        // Given
        String accountId = "ACC001";
        BigDecimal depositAmount = new BigDecimal("150.00");
        TransactionRequest request = new TransactionRequest(depositAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<Account> result = depositUseCase.execute(accountId, request);

        // Then
        assertFalse(result.isPresent());
        
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }

    @Test
    void shouldPropagateExceptionFromTransferUseCase() {
        // Given
        String accountId = "NONEXISTENT";
        BigDecimal depositAmount = new BigDecimal("150.00");
        TransactionRequest request = new TransactionRequest(depositAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenThrow(new IllegalArgumentException("Destination account not found: " + accountId));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> depositUseCase.execute(accountId, request));
        
        assertEquals("Destination account not found: " + accountId, exception.getMessage());
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }
}
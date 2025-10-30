package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.common.exception.AccountNotFoundException;
import com.reckless_bank.in_memory_apis.common.exception.InsufficientFundsException;
import com.reckless_bank.in_memory_apis.common.exception.InvalidTransactionException;
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

class WithdrawUseCaseTest {

    @Mock
    private TransferUseCase transferUseCase;

    @InjectMocks
    private WithdrawUseCase withdrawUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldWithdrawSuccessfully() {
        // Given
        String accountId = "ACC001";
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);
        
        Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("400.00"));
        TransferResult transferResult = new TransferResult(updatedAccount, null);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenReturn(Optional.of(transferResult));

        // When
        Optional<Account> result = withdrawUseCase.execute(accountId, request);

        // Then
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("400.00"), result.get().balance());
        assertEquals(accountId, result.get().accountId());
        
        // Verify that TransferUseCase was called with correct parameters
        verify(transferUseCase, times(1)).execute(argThat(transferRequest -> 
            transferRequest.fromAccountId().equals(accountId) &&
            transferRequest.toAccountId() == null &&
            transferRequest.amount().equals(withdrawAmount)
        ));
    }

    @Test
    void shouldReturnEmptyWhenTransferFails() {
        // Given
        String accountId = "ACC001";
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<Account> result = withdrawUseCase.execute(accountId, request);

        // Then
        assertFalse(result.isPresent());
        
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }

    @Test
    void shouldPropagateAccountNotFoundExceptionFromTransferUseCase() {
        // Given
        String accountId = "NONEXISTENT";
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenThrow(new AccountNotFoundException(accountId));

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
            () -> withdrawUseCase.execute(accountId, request));
        
        assertEquals("Account not found with ID: " + accountId, exception.getMessage());
        assertEquals(accountId, exception.getAccountId());
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }

    @Test
    void shouldPropagateInsufficientFundsExceptionFromTransferUseCase() {
        // Given
        String accountId = "ACC001";
        BigDecimal withdrawAmount = new BigDecimal("1000.00");
        BigDecimal currentBalance = new BigDecimal("500.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenThrow(new InsufficientFundsException(accountId, withdrawAmount, currentBalance));

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, 
            () -> withdrawUseCase.execute(accountId, request));
        
        assertEquals(accountId, exception.getAccountId());
        assertEquals(withdrawAmount, exception.getRequestedAmount());
        assertEquals(currentBalance, exception.getAvailableBalance());
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }

    @Test
    void shouldThrowInvalidTransactionExceptionForNullAccountId() {
        // Given
        String accountId = null;
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);

        // When & Then
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class, 
            () -> withdrawUseCase.execute(accountId, request));
        
        assertTrue(exception.getMessage().contains("Account ID cannot be null or empty"));
        assertEquals("withdrawal", exception.getTransactionType());
        verify(transferUseCase, never()).execute(any(TransferRequest.class));
    }

    @Test
    void shouldThrowInvalidTransactionExceptionForEmptyAccountId() {
        // Given
        String accountId = "   ";
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);

        // When & Then
        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class, 
            () -> withdrawUseCase.execute(accountId, request));
        
        assertTrue(exception.getMessage().contains("Account ID cannot be null or empty"));
        assertEquals("withdrawal", exception.getTransactionType());
        verify(transferUseCase, never()).execute(any(TransferRequest.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidAmountInRequest() {
        // Given - Test that TransactionRequest constructor validation works
        BigDecimal invalidAmount = BigDecimal.ZERO;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> new TransactionRequest(invalidAmount));
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
    }

    @Test
    void shouldPropagateUnexpectedExceptionFromTransferUseCase() {
        // Given
        String accountId = "ACC001";
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        TransactionRequest request = new TransactionRequest(withdrawAmount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
            .thenThrow(new RuntimeException("Unexpected database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> withdrawUseCase.execute(accountId, request));
        
        assertEquals("Unexpected database error", exception.getMessage());
        verify(transferUseCase, times(1)).execute(any(TransferRequest.class));
    }
}
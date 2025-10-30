package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.account.domain.repository.IAccountRepository;
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
import static org.mockito.Mockito.*;

class TransferUseCaseTest {

    @Mock
    private IAccountRepository accountRepository;

    @InjectMocks
    private TransferUseCase transferUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldTransferSuccessfully() {
        // Given
        String fromAccountId = "ACC001";
        String toAccountId = "ACC002";
        BigDecimal transferAmount = new BigDecimal("100.00");
        
        Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("500.00"));
        Account toAccount = new Account(toAccountId, "Jane Smith", new BigDecimal("300.00"));
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        
        TransferRequest request = new TransferRequest(fromAccountId, toAccountId, transferAmount);

        // When
        Optional<TransferResult> result = transferUseCase.execute(request);

        // Then
        assertTrue(result.isPresent());
        TransferResult transferResult = result.get();
        
        assertEquals(new BigDecimal("400.00"), transferResult.fromAccount().balance());
        assertEquals(new BigDecimal("400.00"), transferResult.toAccount().balance());
        
        verify(accountRepository, times(1)).save(transferResult.fromAccount());
        verify(accountRepository, times(1)).save(transferResult.toAccount());
    }

    @Test
    void shouldThrowExceptionWhenFromAccountNotFound() {
        // Given
        String fromAccountId = "NONEXISTENT";
        String toAccountId = "ACC002";
        BigDecimal transferAmount = new BigDecimal("100.00");
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());
        
        TransferRequest request = new TransferRequest(fromAccountId, toAccountId, transferAmount);

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals("Account not found with ID: " + fromAccountId, exception.getMessage());
        assertEquals(fromAccountId, exception.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenToAccountNotFound() {
        // Given
        String fromAccountId = "ACC001";
        String toAccountId = "NONEXISTENT";
        BigDecimal transferAmount = new BigDecimal("100.00");
        
        Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("500.00"));
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());
        
        TransferRequest request = new TransferRequest(fromAccountId, toAccountId, transferAmount);

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals("Account not found with ID: " + toAccountId, exception.getMessage());
        assertEquals(toAccountId, exception.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        // Given
        String fromAccountId = "ACC001";
        String toAccountId = "ACC002";
        BigDecimal transferAmount = new BigDecimal("600.00"); // More than available balance
        
        Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("500.00"));
        Account toAccount = new Account(toAccountId, "Jane Smith", new BigDecimal("300.00"));
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        
        TransferRequest request = new TransferRequest(fromAccountId, toAccountId, transferAmount);

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals(fromAccountId, exception.getAccountId());
        assertEquals(transferAmount, exception.getRequestedAmount());
        assertEquals(new BigDecimal("500.00"), exception.getAvailableBalance());
    }

    @Test
    void shouldPerformDepositWhenFromAccountIsEmpty() {
        // Given
        String toAccountId = "ACC002";
        BigDecimal depositAmount = new BigDecimal("150.00");
        
        Account toAccount = new Account(toAccountId, "Jane Smith", new BigDecimal("300.00"));
        
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        
        TransferRequest request = new TransferRequest(null, toAccountId, depositAmount);

        // When
        Optional<TransferResult> result = transferUseCase.execute(request);

        // Then
        assertTrue(result.isPresent());
        TransferResult transferResult = result.get();
        
        assertNull(transferResult.fromAccount());
        assertEquals(new BigDecimal("450.00"), transferResult.toAccount().balance());
        
        verify(accountRepository, times(1)).save(transferResult.toAccount());
    }

    @Test
    void shouldPerformWithdrawalWhenToAccountIsEmpty() {
        // Given
        String fromAccountId = "ACC001";
        BigDecimal withdrawalAmount = new BigDecimal("200.00");
        
        Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("500.00"));
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        
        TransferRequest request = new TransferRequest(fromAccountId, "", withdrawalAmount);

        // When
        Optional<TransferResult> result = transferUseCase.execute(request);

        // Then
        assertTrue(result.isPresent());
        TransferResult transferResult = result.get();
        
        assertEquals(new BigDecimal("300.00"), transferResult.fromAccount().balance());
        assertNull(transferResult.toAccount());
        
        verify(accountRepository, times(1)).save(transferResult.fromAccount());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalExceedsBalance() {
        // Given
        String fromAccountId = "ACC001";
        BigDecimal withdrawalAmount = new BigDecimal("600.00"); // More than available balance
        
        Account fromAccount = new Account(fromAccountId, "John Doe", new BigDecimal("500.00"));
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        
        TransferRequest request = new TransferRequest(fromAccountId, null, withdrawalAmount);

        // When & Then
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals(fromAccountId, exception.getAccountId());
        assertEquals(withdrawalAmount, exception.getRequestedAmount());
        assertEquals(new BigDecimal("500.00"), exception.getAvailableBalance());
    }

    @Test
    void shouldThrowExceptionWhenDepositAccountNotFound() {
        // Given
        String toAccountId = "NONEXISTENT";
        BigDecimal depositAmount = new BigDecimal("100.00");
        
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.empty());
        
        TransferRequest request = new TransferRequest(null, toAccountId, depositAmount);

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals("Account not found with ID: " + toAccountId, exception.getMessage());
        assertEquals(toAccountId, exception.getAccountId());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalAccountNotFound() {
        // Given
        String fromAccountId = "NONEXISTENT";
        BigDecimal withdrawalAmount = new BigDecimal("100.00");
        
        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.empty());
        
        TransferRequest request = new TransferRequest(fromAccountId, "", withdrawalAmount);

        // When & Then
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, 
            () -> transferUseCase.execute(request));
        
        assertEquals("Account not found with ID: " + fromAccountId, exception.getMessage());
        assertEquals(fromAccountId, exception.getAccountId());
    }
}
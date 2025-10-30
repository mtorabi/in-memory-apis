package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.account.domain.repository.IAccountRepository;
import com.reckless_bank.in_memory_apis.common.exception.AccountNotFoundException;
import com.reckless_bank.in_memory_apis.common.exception.InsufficientFundsException;
import com.reckless_bank.in_memory_apis.common.exception.InvalidTransactionException;
import com.reckless_bank.in_memory_apis.common.exception.RepositoryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransferUseCase {
    
    private final IAccountRepository accountRepository;
    
    public TransferUseCase(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Optional<TransferResult> execute(TransferRequest request) {
        boolean isFromAccountEmpty = request.fromAccountId() == null || request.fromAccountId().trim().isEmpty();
        boolean isToAccountEmpty = request.toAccountId() == null || request.toAccountId().trim().isEmpty();
        
        // Case 1: Deposit - fromAccount is empty, only toAccount is specified
        if (isFromAccountEmpty && !isToAccountEmpty) {
            return executeDeposit(request);
        }
        
        // Case 2: Withdrawal - toAccount is empty, only fromAccount is specified
        if (!isFromAccountEmpty && isToAccountEmpty) {
            return executeWithdrawal(request);
        }
        
        // Case 3: Transfer - both accounts are specified
        if (!isFromAccountEmpty && !isToAccountEmpty) {
            return executeTransfer(request);
        }
        
        // This should not happen due to validation in TransferRequest
        throw new InvalidTransactionException("Invalid transfer request: both accounts cannot be empty");
    }
    
    private Optional<TransferResult> executeDeposit(TransferRequest request) {
        try {
            Optional<Account> toAccountOpt = accountRepository.findById(request.toAccountId());
            
            if (toAccountOpt.isEmpty()) {
                throw new AccountNotFoundException(request.toAccountId());
            }
            
            Account toAccount = toAccountOpt.get();
            Account updatedToAccount = toAccount.deposit(request.amount());
            
            accountRepository.save(updatedToAccount);
            
            return Optional.of(new TransferResult(null, updatedToAccount));
        } catch (AccountNotFoundException ex) {
            throw ex; // Re-throw custom exceptions
        } catch (Exception ex) {
            throw new RepositoryException("deposit", "Account", request.toAccountId(), ex);
        }
    }
    
    private Optional<TransferResult> executeWithdrawal(TransferRequest request) {
        try {
            Optional<Account> fromAccountOpt = accountRepository.findById(request.fromAccountId());
            
            if (fromAccountOpt.isEmpty()) {
                throw new AccountNotFoundException(request.fromAccountId());
            }
            
            Account fromAccount = fromAccountOpt.get();
            
            // Check if the source account has sufficient funds
            if (!fromAccount.hasSufficientFunds(request.amount())) {
                throw new InsufficientFundsException(request.fromAccountId(), request.amount(), fromAccount.balance());
            }
            
            Account updatedFromAccount = fromAccount.withdraw(request.amount());
            
            accountRepository.save(updatedFromAccount);
            
            return Optional.of(new TransferResult(updatedFromAccount, null));
        } catch (AccountNotFoundException | InsufficientFundsException ex) {
            throw ex; // Re-throw custom exceptions
        } catch (Exception ex) {
            throw new RepositoryException("withdrawal", "Account", request.fromAccountId(), ex);
        }
    }
    
    private Optional<TransferResult> executeTransfer(TransferRequest request) {
        try {
            // Find both accounts
            Optional<Account> fromAccountOpt = accountRepository.findById(request.fromAccountId());
            Optional<Account> toAccountOpt = accountRepository.findById(request.toAccountId());
            
            if (fromAccountOpt.isEmpty()) {
                throw new AccountNotFoundException(request.fromAccountId());
            }
            
            if (toAccountOpt.isEmpty()) {
                throw new AccountNotFoundException(request.toAccountId());
            }
            
            Account fromAccount = fromAccountOpt.get();
            Account toAccount = toAccountOpt.get();
            
            // Check if the source account has sufficient funds
            if (!fromAccount.hasSufficientFunds(request.amount())) {
                throw new InsufficientFundsException(request.fromAccountId(), request.amount(), fromAccount.balance());
            }
            
            // Perform the transfer
            Account updatedFromAccount = fromAccount.withdraw(request.amount());
            Account updatedToAccount = toAccount.deposit(request.amount());
            
            // Save both accounts
            accountRepository.save(updatedFromAccount);
            accountRepository.save(updatedToAccount);
            
            return Optional.of(new TransferResult(updatedFromAccount, updatedToAccount));
        } catch (AccountNotFoundException | InsufficientFundsException ex) {
            throw ex; // Re-throw custom exceptions
        } catch (Exception ex) {
            throw new RepositoryException("transfer", "Account", 
                String.format("%s->%s", request.fromAccountId(), request.toAccountId()), ex);
        }
    }
}